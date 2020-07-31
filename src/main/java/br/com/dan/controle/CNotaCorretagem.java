package br.com.dan.controle;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import br.com.dan.servico.SPdf;

@RestController()
@RequestMapping("notas")
public class CNotaCorretagem {
	
	private SPdf sPdf;
	
	@Autowired
	public CNotaCorretagem(SPdf sPdf) {
		this.sPdf = sPdf;
	}
	
	@PostMapping(value = "/importarArquivo", produces = "text/csv")
	public @ResponseBody byte[] importarArquivo(@RequestParam("file") MultipartFile file) throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
		
		System.out.println("Nome: " + file.getName());
		System.out.println("Tamanho: " + file.getSize());
		
	    return this.sPdf.getCsvFromPdf(file.getInputStream());
	}
	
	@PostMapping(value = "/importarArquivos")
	public ResponseEntity<Resource> importarArquivos(@RequestParam("files") MultipartFile[] files) throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
		
	    byte[] file = this.sPdf.getCsvFromPdfs(Arrays.asList(files));
	    
	    InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(file));
	    
	    HttpHeaders headers = new HttpHeaders();
	    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=notas.csv");
	    
	    return ResponseEntity.ok()
	            .headers(headers)
	            .contentLength(file.length)
	            .contentType(MediaType.APPLICATION_OCTET_STREAM)
	            .body(resource);
	}
}
