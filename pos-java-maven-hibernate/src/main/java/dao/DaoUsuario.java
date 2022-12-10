package dao;

import java.util.List;

import javax.persistence.Query;

import model.UsuarioPessoa;

public class DaoUsuario<E> extends DaoGeneric<UsuarioPessoa> {
	
	public void removerUsuario(UsuarioPessoa pessoa) throws Exception {/*Recebe uma entidade de Usuariopessoa, no caso a pessoa*/
		getEntityManager().getTransaction().begin();/*Inicia a uma transação com o banco*/
		
		getEntityManager().remove(pessoa);/*Só pelo cascade = CascadeType.REMOVE, orphanRemoval = true, consegue remover tudo*/
		
		//String sqlDeleteFone = "delete from telefoneuser where usuariopessoa_id = " + pessoa.getId();/*Da o cmoando sql para o banco de dados*/
		//getEntityManager().createNativeQuery(sqlDeleteFone).executeUpdate();/*Faz a alteração, no caso o delete*/
		
		//sqlDeleteFone = "delete from emailuser where usuariopessoa_id = " + pessoa.getId();/*Da o cmoando sql para o banco de dados*/
		//getEntityManager().createNativeQuery(sqlDeleteFone).executeUpdate();/*Faz a alteração, no caso o delete*/
		
		getEntityManager().getTransaction().commit();/*E dps commita*/
		
		super.deletarPorId(pessoa);
		
	}

	public List<UsuarioPessoa> pesquisar(String campoPesquisa) {
		
		Query query = super.getEntityManager().createQuery("from UsuarioPessoa where nome like '%"+campoPesquisa+"%' ");
		
		return query.getResultList();
	}

}
