package br.com.dan.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import com.opencsv.CSVWriter;
import com.opencsv.bean.BeanToCsv;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;


public class Csv {

	public static byte[] gerarCsv(List<?> lista, Class<?> beanClass) throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
	    
	    ColumnPositionMappingStrategy<Object> strat = new ColumnPositionMappingStrategy<>();
	    strat.setType(beanClass);
	    String[] columns = getNomeAtributos(beanClass);
	    strat.setColumnMapping(columns);


	    ByteArrayOutputStream stream = new ByteArrayOutputStream();
	    OutputStreamWriter outputfile = new OutputStreamWriter(stream, StandardCharsets.UTF_8);

	    CSVWriter writer = new CSVWriter(outputfile, ';', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER,
	        CSVWriter.DEFAULT_LINE_END);
	    
	    BeanToCsv<Object> bean = new BeanToCsv<>();

	    
	    if (!bean.write(strat, writer, lista)) {
	    	System.out.println(lista.size());
	      throw new IOException("Erro ao gerar CSV!");
	    }

	    outputfile.flush();

	    return stream.toByteArray();
	  }


	  private static String[] getNomeAtributos(Class<?> c) {
	    String stringArray[] = Arrays.stream(c.getDeclaredFields()).filter(f -> !f.getName().equals("serialVersionUID"))
	        .map(Field::getName).toArray(String[]::new);

	    return stringArray;
	  }
}
