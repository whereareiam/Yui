package me.whereareiam.yue.adapter.database.adapter;

import lombok.AllArgsConstructor;
import me.whereareiam.yue.adapter.database.entity.RoleEntity;
import me.whereareiam.yue.adapter.database.repository.RoleRepository;
import me.whereareiam.yue.api.output.service.RoleService;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RoleServiceAdapter implements RoleService {
	private final RoleRepository roleRepository;

	@Override
	public void addRole(long id) {
		if (!roleExists(id))
			roleRepository.save(
					RoleEntity.builder()
							.id(id)
							.build()
			);
	}

	@Override
	public void removeRole(long id) {
		roleRepository.deleteById(id);
	}

	@Override
	public boolean roleExists(long id) {
		return roleRepository.findById(id).isPresent();
	}

	@Override
	public long[] getAvailableRoles() {
		return roleRepository.findAll()
				.stream()
				.mapToLong(RoleEntity::getId)
				.toArray();
	}
}
