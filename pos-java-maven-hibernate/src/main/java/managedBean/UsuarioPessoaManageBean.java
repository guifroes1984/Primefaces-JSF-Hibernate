package managedBean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.apache.tomcat.util.codec.binary.Base64;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;

import com.google.gson.Gson;

import dao.DaoEmail;
import dao.DaoUsuario;
import datatablelazy.LazyDataTableModelUserPessoa;
import model.EmailUser;
import model.UsuarioPessoa;

@ManagedBean(name = "usuarioPessoaManageBean")
@ViewScoped /*Enquanto estiver com a tela aberta, o ViewScoped vai estar segurando ela alí*/
public class UsuarioPessoaManageBean {

	private UsuarioPessoa usuarioPessoa = new UsuarioPessoa();
	private LazyDataTableModelUserPessoa<UsuarioPessoa> list = new LazyDataTableModelUserPessoa<UsuarioPessoa>();/*Lista de usuários instanciada*/
	private DaoUsuario<UsuarioPessoa> daoGeneric = new DaoUsuario<UsuarioPessoa>();
	private BarChartModel barChartModel = new BarChartModel();/*Objeto do primefaces, responsável por criar o gráfico*/
	private EmailUser emailUser = new EmailUser();
	private DaoEmail<EmailUser> daoEmail = new DaoEmail<EmailUser>();
	private String campoPesquisa;
	
	@PostConstruct/*Dps que UsuarioPessoaManageBean é construído na memória, ele executa o método init apenas uma vez, então ele vai consultar no banco apenas uma vez*/
	public void init() {/*Método inicial. Então consulta no banco só a primeira vez que abrir a tela*/
		list.load(0, 5, null, null, null);/*Quando abrir a tela ja vai carregar os primeiros 5 registros*/
		montarGrafico();
		
	}

	private void montarGrafico() {
		barChartModel = new BarChartModel();
		
		ChartSeries userSalario = new ChartSeries();/*Grupo de funcionários*/
		
		for (UsuarioPessoa usuarioPessoa : list.list) {/*Adiciona o salario para o grupo*/
			userSalario.set(usuarioPessoa.getNome(), usuarioPessoa.getSalario());/*Adiciona salários*/
		}
		
		barChartModel.addSeries(userSalario);
		barChartModel.setTitle("Gráfico de salários");
	}
	
	public BarChartModel getBarChartModel() {
		return barChartModel;
	}
	
	public void pesquisaCep(AjaxBehaviorEvent event) {/*Toda vez que for trabalhar com Ajax tem que passar esse parametro=> AjaxBehaviorEvent event*/
		try {
			
			URL url = new URL("https://viacep.com.br/ws/"+usuarioPessoa.getCep()+"/json/");
			URLConnection connection = url.openConnection();
			InputStream is = connection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			
			String cep = "";
			StringBuilder jsoCep = new StringBuilder();
			
			while ((cep = br.readLine()) != null) {
				jsoCep.append(cep);
			}
			
			UsuarioPessoa userCepPessoa = new Gson().fromJson(jsoCep.toString(), UsuarioPessoa.class);
			
			usuarioPessoa.setCep(userCepPessoa.getCep());
			usuarioPessoa.setLogradouro(userCepPessoa.getLogradouro());
			usuarioPessoa.setComplemento(userCepPessoa.getComplemento());
			usuarioPessoa.setBairro(userCepPessoa.getBairro());
			usuarioPessoa.setLocalidade(userCepPessoa.getLocalidade());
			usuarioPessoa.setUf(userCepPessoa.getUf());
			usuarioPessoa.setUnidade(userCepPessoa.getUnidade());
			usuarioPessoa.setIbge(userCepPessoa.getIbge());
			usuarioPessoa.setGia(userCepPessoa.getGia());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String salvar() {
		daoGeneric.salvar(usuarioPessoa);/*Salva no banco de dados*/
		list.list.add(usuarioPessoa);/*Adicionar o objeto na lista*/
		usuarioPessoa = new UsuarioPessoa();/*Salva e já instancia um novo usuário*/
		init();/*Vai redenrizar o grafico com os salários atualizados*/
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Informação: ", "Salvo com sucesso!"));
		return "";
	}
	
	public String novo() {
		usuarioPessoa = new UsuarioPessoa();
		return "";
	}
	
	public LazyDataTableModelUserPessoa<UsuarioPessoa> getList() {/*Quando vai carregar uma lista de pessoas, só precisa do getList*/
		montarGrafico();
		return list;
	}
	
	public String remover() {
		
		try {
			daoGeneric.removerUsuario(usuarioPessoa);/*Removeu do banco*/
			list.list.remove(usuarioPessoa);/*Remove da lista da tela*/
			usuarioPessoa = new UsuarioPessoa();/*Instancia uma nova pessoa*/
			init();/*Vai redenrizar o grafico com os salários atualizados*/
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Informação: ", "Removido com sucesso!"));
		
		} catch (Exception e) {
			if (e.getCause()instanceof org.hibernate.exception.ConstraintViolationException) {
				FacesContext.getCurrentInstance().addMessage(null, 
						new FacesMessage(FacesMessage.SEVERITY_INFO, 
								"Informação: ", "Existem telefone para o usuário"));
			}else {
				e.printStackTrace();
			}
		}
		return "";
	}
	
	public void addEmail() {
		emailUser.setUsuarioPessoa(usuarioPessoa);/*Adicionou a pessoa, amarrou a pessoa*/
		emailUser = daoEmail.updateMerge(emailUser);/*emailUser=> Esse objeto já vai ter PK que gravou no banco e voltou, já vai ter a pessoa amarrada pela descrição do e-mail*/
		usuarioPessoa.getEmails().add(emailUser);/*Adicionou o objeto novo na lista*/
		emailUser = new EmailUser();/*Fica pronto para cadastrar um novo e-mail*/
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Resultado: ", "Salvo com sucesso!"));
	}
	
	public void removerEmail() throws Exception {
		String codigoemail = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("codigoemail");/*Vai pegar esse parâmetro que vem da tela como "codigoemail". Peguei o código do e-mail que quero remover*/
		
		EmailUser remover = new EmailUser();/*Instância o objeto*/
		remover.setId(Long.parseLong(codigoemail));/*Passando o "id" do objeto, pq tenho que ter apenas o id para remover*/
		daoEmail.deletarPorId(remover);/*Passo para o método Generico para deletar por id, recebendo a entidade, pegando o código e remove do BD*/
		usuarioPessoa.getEmails().remove(remover);/*Ai remove da lista em memória*/
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Resultado: ", "Removido com sucesso!"));
	}
	
	public void pesquisar() {
		list.pesquisar(campoPesquisa);/*É a lista filtrada*/
		montarGrafico();/*Vai usar a lista quando ela estiver filtrada pelo nome*/
		
	}
	
	public void upload(FileUploadEvent image) {/*Quando vai trabalhar com imagem de upload tem que invocar FileUploadEvent*/
		String imagem = "data:imagem/png;base64," + DatatypeConverter.printBase64Binary(image.getFile().getContents());/*Imagem convertida para basee 64*/
		usuarioPessoa.setImagem(imagem);/*Setar para o objeto pessoa o atibuto imagem*/
	}
	
	public void download() throws IOException {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String fileDownloadID = params.get("fileDownloadId");
		
		UsuarioPessoa pessoa = daoGeneric.pesquisar(Long.parseLong(fileDownloadID), UsuarioPessoa.class);
		
		byte[] imagem = new Base64().decodeBase64(pessoa.getImagem().split("\\,")[1]);
		
		HttpServletResponse response = (HttpServletResponse)FacesContext.getCurrentInstance().getExternalContext().getResponse();
		
		response.addHeader("Content-Disposition","attachment; filename=download.png");
		response.setContentType("application/octet-stream");
		response.setContentLength(imagem.length);
		response.getOutputStream().write(imagem);
		response.getOutputStream().flush();
		FacesContext.getCurrentInstance().responseComplete();
	}
	

	public UsuarioPessoa getUsuarioPessoa() {
		return usuarioPessoa;
	}

	public void setUsuarioPessoa(UsuarioPessoa usuarioPessoa) {
		this.usuarioPessoa = usuarioPessoa;
	}
	
	public void setEmailUser(EmailUser emailUser) {
		this.emailUser = emailUser;
	}
	
	public EmailUser getEmailUser() {
		return emailUser;
	}
	
	public void setCampoPesquisa(String campoPesquisa) {
		this.campoPesquisa = campoPesquisa;
	}
	
	public String getCampoPesquisa() {
		return campoPesquisa;
	}

}
