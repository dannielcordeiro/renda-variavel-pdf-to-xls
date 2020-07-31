package br.com.dan.servico;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import br.com.dan.bean.BOperacao;
import br.com.dan.util.Csv;

@Service
public class SPdf {

	public byte[] getCsvFromPdf(InputStream is) throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, IOException {
		return Csv.gerarCsv(this.getOperacoesFromPdf(is), BOperacao.class);
	}
	
	public byte[] getCsvFromPdfs(List<MultipartFile> files) throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, IOException {
		List<BOperacao> lista = new ArrayList<>();
		
		for(MultipartFile file : files) {
			lista.addAll(this.getOperacoesFromPdf(file.getInputStream()));
		}
		
		return Csv.gerarCsv(lista, BOperacao.class);
	}
	
	private List<BOperacao> getOperacoesFromPdf(InputStream is) throws IOException{
		List<String> paginas = getTextOnDocument(is);
		List<BOperacao> operacoes = new ArrayList<>();
		for(String pagina : paginas) {
			operacoes.addAll(this.getOperacoesPagina(pagina));
		}
		
		return operacoes;
	}
	
	private List<BOperacao> getOperacoesPagina(String pagina){
		List<BOperacao> operacoes = new ArrayList<>();
		
		String reDataLines = "[0-9]+-BOV[A-Z 0-9,\\.]*\r*\n";
	    Pattern ptDataLines = Pattern.compile(reDataLines);
	    Matcher m = ptDataLines.matcher(pagina);
  
	    while(m.find()) {	        
	    	BOperacao operacao = this.getOperacaoFromLinha(m.group());
	    	operacao.setDataPregao(this.getDataNota(pagina));
	    	operacoes.add(operacao);
	    }
	    
	    operacoes.addAll(this.getDayTradePagina(pagina));
		
		return operacoes;
	}
	
	private List<BOperacao> getDayTradePagina(String pagina){
		List<BOperacao> operacoes = new ArrayList<>();
		
		String reDataLines = "[-/@\\., a-zA-Z0-9]*DAY TRADE[-\\., a-zA-Z0-9]*\r*\n";
	    Pattern ptDataLines = Pattern.compile(reDataLines);
	    Matcher m = ptDataLines.matcher(pagina);
  
	    while(m.find()) {	        
	    	BOperacao operacao = this.getDaytradeFromLinha(m.group());
	    	operacao.setDataPregao(this.getDataNota(pagina));
	    	operacoes.add(operacao);
	    }
		
		return operacoes;
	}
	
	private BOperacao getOperacaoFromLinha(String linhaOperacao) {
		List<String> linhaSplit = new ArrayList<>(Arrays.asList(linhaOperacao.split(" ")));

		BOperacao operacao = new BOperacao();
		operacao.setPapel(this.getNomePapel(linhaOperacao));
		operacao.setTipoOperacao(linhaSplit.get(1).charAt(0));
		operacao.setTipoMercado(linhaSplit.get(2));
		linhaSplit.remove(0);
		linhaSplit.remove(0);
		linhaSplit.remove(0);
		
		String descricao = new String();
		String proximoCampo = new String();
		Pattern ptDigitos = Pattern.compile("[0-9]+");
		Matcher m = ptDigitos.matcher(proximoCampo);
		for(Iterator<String> iterator = linhaSplit.iterator(); iterator.hasNext();) {
			proximoCampo = iterator.next();
			m = ptDigitos.matcher(proximoCampo);
			if(m.matches()) break;
			descricao = descricao.concat(new String(proximoCampo.strip())).concat(" ");
			iterator.remove();
		}
		
		operacao.setDescricaoPapel(descricao.strip());
		operacao.setQuantidade(Integer.valueOf(linhaSplit.get(0)));	
		operacao.setValorCota(new BigDecimal(linhaSplit.get(1).replaceAll("\\.", "").replaceAll(",", ".")));	
		operacao.setValorTotal(new BigDecimal(linhaSplit.get(2).replaceAll("\\.", "").replaceAll(",", ".")));
		operacao.setCreditoOuDebito(linhaSplit.get(3).charAt(0));
		
		return operacao;
	}
	
	private BOperacao getDaytradeFromLinha(String linhaOperacao) {
		List<String> linhaSplit = new ArrayList<>(Arrays.asList(linhaOperacao.split(" ")));

		BOperacao operacao = new BOperacao();
		operacao.setTipoOperacao(linhaSplit.get(0).charAt(0));
		operacao.setTipoMercado("DAYTRADE");
		operacao.setDescricaoPapel(linhaSplit.get(1).concat(linhaSplit.get(2)));
		operacao.setPapel("");
		operacao.setQuantidade(Integer.valueOf(linhaSplit.get(4)));	
		operacao.setValorCota(new BigDecimal(linhaSplit.get(5).replaceAll("\\.", "").replaceAll(",", ".")));	
		operacao.setValorTotal(new BigDecimal(linhaSplit.get(8).replaceAll("\\.", "").replaceAll(",", ".")));
		operacao.setCreditoOuDebito(linhaSplit.get(9).charAt(0));	
		
		return operacao;
	}
	
	private List<String> getTextOnDocument(InputStream is) throws IOException {
		 List<String> paginas = new ArrayList<>();
		 
		 try (PDDocument document = new PDDocument().load(is)) {
		        
		   PDFTextStripper stripper = new PDFTextStripper();
		   stripper.setSortByPosition(true);
		
		   PDPageTree pages = document.getPages();
		   Iterator<PDPage> i = pages.iterator();
		   
		  
		   for (int p = 1; p <= document.getNumberOfPages(); ++p) {
		   	stripper.setStartPage(p);
		       stripper.setEndPage(p);
		
		       String text = stripper.getText(document);
		       paginas.add(text);
		   }
		   
		   document.close();
		   
		 }
		 
		 is.close();
		 
		 return paginas;
 
	 }
	
	private String getDataNota(String nota) {
		String reLinhaDateNota = "pregão\r*\n*[0-9\\. /]*\r*\n";
		Pattern ptLinhaDateNota = Pattern.compile(reLinhaDateNota);
		String reDateNota = "\\d\\d/\\d\\d/\\d\\d\\d\\d";
		Pattern ptDateNota = Pattern.compile(reDateNota);
      
		Matcher m1 = ptLinhaDateNota.matcher(nota);
		if(m1.find()) {
			Matcher m2 = ptDateNota.matcher(m1.group());
			if(m2.find()) return m2.group();
		}
		
		return null;
	}
	
	private String getNomePapel(String line) {
		String papel = new String();
		
		Pattern ptPapel = Pattern.compile(" [A-Z]{4}[123456]{1,2}");
		Matcher mPapel = ptPapel.matcher(line);
		if(mPapel.find()) {
			papel = mPapel.group();
		}
		
		return papel;
	}
}
