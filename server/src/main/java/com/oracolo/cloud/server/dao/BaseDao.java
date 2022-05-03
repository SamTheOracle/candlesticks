package com.oracolo.cloud.server.dao;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@ApplicationScoped
public class BaseDao<T> {

	@Inject
	EntityManager em;

	public void insert(T entity) {
		em.persist(entity);
	}

	public void update(T entity) {
		em.merge(entity);
	}

	public void delete(T entity) {
		em.remove(entity);
	}

	public Optional<T> getById(Object id, Class<T> type) {
		return Optional.ofNullable(em.find(type, id));
	}
}

