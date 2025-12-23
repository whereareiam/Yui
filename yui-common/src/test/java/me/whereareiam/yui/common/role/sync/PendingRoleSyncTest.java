package me.whereareiam.yui.common.role.sync;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

class PendingRoleSyncTest {

	@Test
	void shouldAddRole() {
		PendingRoleSync sync = new PendingRoleSync(123L);
		sync.addRole(100L);

		assertThat(sync.getRolesToAdd()).contains(100L);
		assertThat(sync.getRolesToRemove()).isEmpty();
	}

	@Test
	void shouldRemoveRole() {
		PendingRoleSync sync = new PendingRoleSync(123L);
		sync.removeRole(100L);

		assertThat(sync.getRolesToRemove()).contains(100L);
		assertThat(sync.getRolesToAdd()).isEmpty();
	}

	@Test
	void shouldOptimizeConflictingOperations_AddThenRemove() {
		// Add → Remove same role = both cancelled
		PendingRoleSync sync = new PendingRoleSync(123L);
		sync.addRole(100L);
		sync.removeRole(100L);

		assertThat(sync.getRolesToAdd()).isEmpty();
		assertThat(sync.getRolesToRemove()).isEmpty();
	}

	@Test
	void shouldOptimizeConflictingOperations_RemoveThenAdd() {
		// Remove → Add same role = just add
		PendingRoleSync sync = new PendingRoleSync(123L);
		sync.removeRole(100L);
		sync.addRole(100L);

		assertThat(sync.getRolesToAdd()).contains(100L);
		assertThat(sync.getRolesToRemove()).isEmpty();
	}

	@Test
	void shouldOptimizeMultipleAddOperations() {
		// Adding same role multiple times = single add
		PendingRoleSync sync = new PendingRoleSync(123L);
		sync.addRole(100L);
		sync.addRole(100L);
		sync.addRole(100L);

		assertThat(sync.getRolesToAdd()).containsExactly(100L);
	}

	@Test
	void shouldOptimizeMultipleRemoveOperations() {
		// Removing same role multiple times = single remove
		PendingRoleSync sync = new PendingRoleSync(123L);
		sync.removeRole(100L);
		sync.removeRole(100L);
		sync.removeRole(100L);

		assertThat(sync.getRolesToRemove()).containsExactly(100L);
	}

	@Test
	void shouldHandleComplexSequence() {
		// Add A → Remove B → Add C → Remove A → Add B
		// Result: Add C, Add B (A cancelled, B cancelled removal)
		PendingRoleSync sync = new PendingRoleSync(123L);
		sync.addRole(100L);    // Add A
		sync.removeRole(200L); // Remove B
		sync.addRole(300L);    // Add C
		sync.removeRole(100L); // Remove A (cancels add A)
		sync.addRole(200L);    // Add B (cancels remove B)

		assertThat(sync.getRolesToAdd()).containsExactlyInAnyOrder(200L, 300L);
		assertThat(sync.getRolesToRemove()).isEmpty();
	}

	@Test
	void shouldTrackLastModified() throws InterruptedException {
		PendingRoleSync sync = new PendingRoleSync(123L);
		Instant before = sync.getLastModified();

		Thread.sleep(10);
		sync.addRole(100L);

		assertThat(sync.getLastModified()).isAfter(before);
	}

	@Test
	void shouldUpdateLastModifiedOnRemove() throws InterruptedException {
		PendingRoleSync sync = new PendingRoleSync(123L);
		sync.addRole(100L);
		Instant afterAdd = sync.getLastModified();

		Thread.sleep(10);
		sync.removeRole(200L);

		assertThat(sync.getLastModified()).isAfter(afterAdd);
	}

	@Test
	void shouldNotBeReadyBeforeDebounce() {
		PendingRoleSync sync = new PendingRoleSync(123L);
		sync.addRole(100L);

		assertThat(sync.isReadyToProcess(500)).isFalse();
	}

	@Test
	void shouldBeReadyAfterDebounce() throws InterruptedException {
		PendingRoleSync sync = new PendingRoleSync(123L);
		sync.addRole(100L);

		Thread.sleep(600);

		assertThat(sync.isReadyToProcess(500)).isTrue();
	}

	@Test
	void shouldNotBeReadyIfAlreadyProcessing() throws InterruptedException {
		PendingRoleSync sync = new PendingRoleSync(123L);
		sync.addRole(100L);
		Thread.sleep(600);

		sync.markProcessing();

		assertThat(sync.isReadyToProcess(500)).isFalse();
	}

	@Test
	void shouldNotBeReadyIfNoPendingChanges() throws InterruptedException {
		PendingRoleSync sync = new PendingRoleSync(123L);
		Thread.sleep(600);

		assertThat(sync.isReadyToProcess(500)).isFalse();
	}

	@Test
	void shouldHavePendingChangesAfterAdd() {
		PendingRoleSync sync = new PendingRoleSync(123L);
		sync.addRole(100L);

		assertThat(sync.hasPendingChanges()).isTrue();
	}

	@Test
	void shouldHavePendingChangesAfterRemove() {
		PendingRoleSync sync = new PendingRoleSync(123L);
		sync.removeRole(100L);

		assertThat(sync.hasPendingChanges()).isTrue();
	}

	@Test
	void shouldNotHavePendingChangesInitially() {
		PendingRoleSync sync = new PendingRoleSync(123L);

		assertThat(sync.hasPendingChanges()).isFalse();
	}

	@Test
	void shouldConsumeRolesToAdd() {
		PendingRoleSync sync = new PendingRoleSync(123L);
		sync.addRole(100L);
		sync.addRole(200L);

		Set<Long> consumed = sync.consumeRolesToAdd();

		assertThat(consumed).containsExactlyInAnyOrder(100L, 200L);
		assertThat(sync.getRolesToAdd()).isEmpty(); // Consumed = cleared
	}

	@Test
	void shouldConsumeRolesToRemove() {
		PendingRoleSync sync = new PendingRoleSync(123L);
		sync.removeRole(100L);
		sync.removeRole(200L);

		Set<Long> consumed = sync.consumeRolesToRemove();

		assertThat(consumed).containsExactlyInAnyOrder(100L, 200L);
		assertThat(sync.getRolesToRemove()).isEmpty(); // Consumed = cleared
	}

	@Test
	void shouldReturnEmptySetWhenNothingToConsume() {
		PendingRoleSync sync = new PendingRoleSync(123L);

		Set<Long> adds = sync.consumeRolesToAdd();
		Set<Long> removes = sync.consumeRolesToRemove();

		assertThat(adds).isEmpty();
		assertThat(removes).isEmpty();
	}

	@Test
	void shouldHandleConcurrentModifications() throws InterruptedException {
		PendingRoleSync sync = new PendingRoleSync(123L);
		CountDownLatch latch = new CountDownLatch(2);

		// Simulate concurrent role additions
		Thread t1 = new Thread(() -> {
			for (int i = 0; i < 100; i++) {
				sync.addRole(i);
			}
			latch.countDown();
		});

		// Simulate concurrent role removals
		Thread t2 = new Thread(() -> {
			for (int i = 0; i < 100; i++) {
				sync.removeRole(i);
			}
			latch.countDown();
		});

		t1.start();
		t2.start();
		latch.await();

		// Should handle concurrent access without errors or data corruption
		assertThat(sync.getRolesToAdd()).isNotNull();
		assertThat(sync.getRolesToRemove()).isNotNull();
		// Due to optimization, add+remove same ID should cancel out
		assertThat(sync.getRolesToAdd()).isEmpty();
		assertThat(sync.getRolesToRemove()).isEmpty();
	}

	@Test
	void shouldHandleConcurrentConsumption() throws InterruptedException {
		PendingRoleSync sync = new PendingRoleSync(123L);
		for (int i = 0; i < 10; i++) {
			sync.addRole(i);
		}

		CountDownLatch latch = new CountDownLatch(2);

		// Two threads trying to consume at the same time
		Thread t1 = new Thread(() -> {
			sync.consumeRolesToAdd();
			latch.countDown();
		});

		Thread t2 = new Thread(() -> {
			sync.consumeRolesToAdd();
			latch.countDown();
		});

		t1.start();
		t2.start();
		latch.await();

		// After both consume, should be empty
		assertThat(sync.getRolesToAdd()).isEmpty();
	}

	@Test
	void shouldPreserveUserIdThroughoutLifecycle() {
		PendingRoleSync sync = new PendingRoleSync(123L);
		sync.addRole(100L);
		sync.removeRole(200L);
		sync.markProcessing();

		assertThat(sync.getUserId()).isEqualTo(123L);
	}
}

