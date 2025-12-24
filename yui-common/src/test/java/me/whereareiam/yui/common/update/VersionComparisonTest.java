package me.whereareiam.yui.common.update;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for version comparison logic in UpdateScheduler.
 */
class VersionComparisonTest {
    @Test
    void testIsReleaseVersion_validVersions() {
        // Valid release versions
        assertTrue(UpdateScheduler.isReleaseVersion("1.0.0"));
        assertTrue(UpdateScheduler.isReleaseVersion("1.2.3"));
        assertTrue(UpdateScheduler.isReleaseVersion("10.20.30"));
        assertTrue(UpdateScheduler.isReleaseVersion("1.0.0-RC1"));
        assertTrue(UpdateScheduler.isReleaseVersion("1.2.3-SNAPSHOT"));
        assertTrue(UpdateScheduler.isReleaseVersion("2.0.0-beta.1"));
    }

    @Test
    void testIsReleaseVersion_invalidVersions() {
        // Invalid versions
        assertFalse(UpdateScheduler.isReleaseVersion("dev"));
        assertFalse(UpdateScheduler.isReleaseVersion("DEV"));
        assertFalse(UpdateScheduler.isReleaseVersion("main-abc123"));
        assertFalse(UpdateScheduler.isReleaseVersion("feature-branch"));
        assertFalse(UpdateScheduler.isReleaseVersion("1.0"));
        assertFalse(UpdateScheduler.isReleaseVersion("1"));
        assertFalse(UpdateScheduler.isReleaseVersion("abc"));
    }

    @Test
    void testBothPureSemver_validCombinations() {
        // Both pure semver
        assertTrue(UpdateScheduler.bothPureSemver("1.0.0", "1.0.1"));
        assertTrue(UpdateScheduler.bothPureSemver("2.5.3", "2.5.4"));
        assertTrue(UpdateScheduler.bothPureSemver("10.20.30", "10.20.31"));
    }

    @Test
    void testBothPureSemver_invalidCombinations() {
        // One has suffix
        assertFalse(UpdateScheduler.bothPureSemver("1.0.0-RC1", "1.0.0"));
        assertFalse(UpdateScheduler.bothPureSemver("1.0.0", "1.0.0-SNAPSHOT"));
        assertFalse(UpdateScheduler.bothPureSemver("1.0.0-alpha", "1.0.0-beta"));

        // Invalid formats
        assertFalse(UpdateScheduler.bothPureSemver("1.0", "1.0.0"));
        assertFalse(UpdateScheduler.bothPureSemver("dev", "1.0.0"));
    }

    @Test
    void testCompareSemver_equality() {
        assertEquals(0,UpdateScheduler.compareSemver("1.0.0", "1.0.0"));
        assertEquals(0, UpdateScheduler.compareSemver("2.5.10", "2.5.10"));
        assertEquals(0, UpdateScheduler.compareSemver("10.20.30", "10.20.30"));
    }

    @Test
    void testCompareSemver_majorVersionDifference() {
        // 2.0.0 > 1.0.0
        assertTrue(UpdateScheduler.compareSemver("2.0.0", "1.0.0") > 0);
        assertTrue(UpdateScheduler.compareSemver("1.0.0", "2.0.0") < 0);

        // 10.0.0 > 9.0.0
        assertTrue(UpdateScheduler.compareSemver("10.0.0", "9.0.0") > 0);
    }

    @Test
    void testCompareSemver_minorVersionDifference() {
        // 1.1.0 > 1.0.0
        assertTrue(UpdateScheduler.compareSemver("1.1.0", "1.0.0") > 0);
        assertTrue(UpdateScheduler.compareSemver("1.0.0", "1.1.0") < 0);

        // 1.10.0 > 1.9.0 (numeric comparison, not lexicographic)
        assertTrue(UpdateScheduler.compareSemver("1.10.0", "1.9.0") > 0);
    }

    @Test
    void testCompareSemver_patchVersionDifference() {
        // 1.0.1 > 1.0.0
        assertTrue(UpdateScheduler.compareSemver("1.0.1", "1.0.0") > 0);
        assertTrue(UpdateScheduler.compareSemver("1.0.0", "1.0.1") < 0);

        // 1.0.10 > 1.0.9 (numeric comparison)
        assertTrue(UpdateScheduler.compareSemver("1.0.10", "1.0.9") > 0);
    }

    @Test
    void testCompareSemver_complexScenarios() {
        // Major version takes precedence
        assertTrue(UpdateScheduler.compareSemver("2.0.0", "1.99.99") > 0);

        // Minor version takes precedence over patch
        assertTrue(UpdateScheduler.compareSemver("1.1.0", "1.0.99") > 0);

        // Real-world scenarios
        assertTrue(UpdateScheduler.compareSemver("1.2.3", "1.2.2") > 0);
        assertTrue(UpdateScheduler.compareSemver("2.0.0", "1.9.9") > 0);
        assertTrue(UpdateScheduler.compareSemver("1.10.0", "1.9.0") > 0);
    }
}
