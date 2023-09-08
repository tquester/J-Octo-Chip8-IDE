package disass;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.Charset;

public class Tools {
	static public void writeTextFile(String filename, String text) {
		  FileOutputStream stream = null;
		    PrintStream out = null;
		    try {
		        File file = new File(filename);
		        stream = new FileOutputStream(file); 
		        out = new PrintStream(stream);
		        out.print(text);                  //This will overwrite existing contents

		    } catch (Exception ex) {
		        ex.printStackTrace();
		    } finally {
		        try {
		            if(stream!=null) stream.close();
		            if(out!=null) out.close();
		        } catch (Exception ex) {
		            //do something
		        }
		    }
	}
	
	static public String loadTextFile(String filename) {
		File file = new File(filename);
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filename));
		    String line = br.readLine();
		    while (line != null) {
		        sb.append(line);
		        sb.append(System.lineSeparator());
		        line = br.readLine();
		    }
		    String everything = sb.toString();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} finally {
		    try {
		    	if (br != null)
		    		br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public static void saveTextFile(String text, String filename) {
		System.out.println("Schreibe: "+filename);
		try {

	        File file = new File(filename);
	 
	        file.delete();
			
		} catch (Exception e) {
		}
		try {
			File file = new File(filename);
			OutputStream os = new FileOutputStream(file);
			OutputStreamWriter writer = new OutputStreamWriter(os, Charset.defaultCharset());
			BufferedWriter bufWriter = new BufferedWriter(writer);
			bufWriter.write(text);
			bufWriter.close();
			writer.close();
			os.close();
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeBinary(byte[] bytes, String filename) {
		try {
			
		     OutputStream outputtStream = new FileOutputStream(filename);
		     outputtStream.write(bytes);
		     outputtStream.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

}
