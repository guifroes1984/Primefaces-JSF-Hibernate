package dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import posjavamavenhibernate.HibernateUtil;

public class DaoGeneric<E> {
	
	private EntityManager entityManager = HibernateUtil.geEntityManager();
	
	/*Método de Salvar*/
	public void salvar(E entidade) {
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		entityManager.persist(entidade);
		transaction.commit();
	}
	
	/*-----------------------------------------------------------------------------------------------------------*/
	
	
	/*Método de fazer update, salva ou atualiza*/
	public E updateMerge(E entidade) {
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		E entidadesalva = entityManager.merge(entidade);
		transaction.commit();
		
		return entidadesalva;
	}
	
	/*-----------------------------------------------------------------------------------------------------------*/
	
	/*Método de pesquisar pasando um parametro*/
	public E pesquisar(E entidade) {
		Object id = HibernateUtil.getPrimaryKey(entidade);
		E e = (E) entityManager.find(entidade.getClass(), id);
		
		return e;
		
	}
	
	/*-----------------------------------------------------------------------------------------------------------*/
	
	/*Método de pesquisar passando 2 paramentros, id e entidade*/
	public E pesquisar(Long id, Class<E> entidade) {
		entityManager.clear();
		E e = (E) entityManager.createQuery("from " + entidade.getSimpleName() + " where id = " + id).getSingleResult();

		return e;

	}
	
	/*-----------------------------------------------------------------------------------------------------------*/
	
	/*Método para deletar por id*/
	public void deletarPorId(E entidade) throws Exception {
		
		Object id = HibernateUtil.getPrimaryKey(entidade); // Obtem o ID do objeto PK
		
		EntityTransaction transaction = entityManager.getTransaction();// Obejeto de transação
		transaction.begin();// Começa uma Transação no banco de dados

		
		entityManager.createNativeQuery("delete from " + entidade.getClass(). // Monta a Query para delete
				getSimpleName().toLowerCase() + " where id =" + id).executeUpdate(); // Executa o delete no banco de  dados
		
		transaction.commit();// Grava alteração no banco
		
	}
	
	/*-----------------------------------------------------------------------------------------------------------*/
	
	public List<E> listar(Class<E> entidade) {
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		
		List<E> lista = entityManager.createQuery("from " + entidade.getName()).getResultList();
		
		transaction.commit();
		
		return lista;
	}
	
	/*-----------------------------------------------------------------------------------------------------------*/
	
	/*Consigo acessar EntityManager de outras partes do sistema*/
	public EntityManager getEntityManager() {
		return entityManager;
	}

	
}
