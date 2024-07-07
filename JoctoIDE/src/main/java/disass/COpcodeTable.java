package disass;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.swing.plaf.metal.MetalIconFactory.FileIcon16;

import org.json.JSONArray;
import org.json.JSONObject;

public class COpcodeTable {
	
	public enum Dialect {
		OCTO,
		CHIPPER
	}
	
	public class Chip8Opcode {
		public int		opcode;			// example 1000
		public int		mask;			// example F000
		public int		size;			// example 2
		public String	opcodePattern;	// example  1nnn
		public String   chipper;		// example JP NNN
		public String 	octo;			// example JUMP NNN
		private String description;
		public TreeSet<String> plattforms;
		public void parse(String pattern, int mask, int size, JSONArray plattforms, String description) {
			this.size = size;
			this.opcodePattern = pattern;
			this.mask = mask;
			this.description = description;
			this.size = size;
			this.plattforms = new TreeSet<String>();
			if (plattforms != null) {
				for (int i=0;i<plattforms.length();i++) {
					String str = plattforms.getString(i);
					this.plattforms.add(str);
				}
			}
			String str="";
			
			for (int i=0;i<pattern.length();i++) {
				char c = pattern.charAt(i);
				if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))
					str += c;
				else 
					str += "0";
			}
			opcode = Integer.parseInt(str,16);
		}
		public String toString() {
			return String.format("%s %04x %04x %s", opcodePattern, opcode, mask, octo); 
		}
		
		boolean match(int op) {
			return (op & mask) == opcode;
		}
		
		String decode(int opcode, TreeMap<Integer, CC8Label> labels, String template) {
			String result = null;
			String strop = String.format("%04x",opcode);
			char c;
			char p;
			int x=0;
			int y=0;
			int nnn=0;
			for (int i=0;i<strop.length();i++) 
			{
				p = opcodePattern.charAt(i);
				c = strop.charAt(i);
				switch(p) {
					case 'x':
					case 'X': x = nibble(c);
						break;
					case 'y':
					case 'Y': y = nibble(c);
					break;
					case 'n': 
						nnn = 16 * nnn + nibble(c);
						break;
				}
			}
			String strlbl = String.format("0x%04x", nnn);
			CC8Label label = labels.get(nnn);
			if (label != null)
				strlbl = label.toString();
			String strn = String.format("0x%x", nnn);
			String strnnn = String.format("%x", nnn);
			String strx = String.format("%x", x);
			String stry = String.format("%x", y);
			if (template != null) 
				result = template.replace("NNN", strlbl).replaceAll("NN", strn).replace("N", strn).replace("X", strx).replace("Y", stry);
			else
				result = String.format("0x04x", opcode);
			return result;
		}
		public String getPlattforms() {
			String str = null;
			for (String x : plattforms) {
				if (str == null) str = x; else str += ", "+x; 
			}
			// TODO Auto-generated method stub
			if (str == null) str = "-";
			return str;
		}
		public String getDescription() {
			return description == null ? "" : description;
		}
	}

	private static final String FILE_URL = null;
	
	public ArrayList<Chip8Opcode> mOpcodes = new ArrayList<>();
	
	public COpcodeTable() {
		try {
		  String jsonString = getChip8Table();
			
		/*	
		  InputStream inputStream = getClass().getClassLoader().getResourceAsStream("chip8.json");
		  String jsonString = new BufferedReader(
			      new InputStreamReader(inputStream, StandardCharsets.UTF_8))
			        .lines()
			        .collect(Collectors.joining("\n"));
		*/
		  JSONObject obj = new JSONObject(jsonString);
		  JSONArray opcodeArray = obj.getJSONArray("opcodes"); // notice that `"posts": [...]`
		  for (int i = 0; i < opcodeArray.length(); i++)
		  {
			  try {
				  JSONObject  jop = opcodeArray.getJSONObject(i);
				  Chip8Opcode op = new Chip8Opcode();
				  mOpcodes.add(op);
				  op.parse(
						  jop.getString("opcode"), 
						  jop.getInt("mask"), 
						  jop.getInt("size"),
						  jop.getJSONArray("platforms"),
						  jop.getString("description")
		
						  );
			      op.chipper = getString(jop,"chipper");
			      op.octo = getString(jop, "octo");
			  }
			  catch(Exception e) {
				  e.printStackTrace();
			  }
		  }


		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		

	}
	
	// https://chip8.gulrak.net/data/opcode-table.json
	private String getChip8Table() {
		File file = new File("opcode-table.json");
		System.out.println("Opcode table "+file.getAbsolutePath());
		if (!file.exists()) {
			System.out.println("File not found, downloading");
			downloadOpcodeTable();
		}
		file = new File("opcode-table.json");
		if (!file.exists()) {
			System.out.println("File still not found, exit");
			return null;
		}
		
		String jsonString = Tools.loadTextFile("opcode-table.json");
		
		return jsonString;
	}


	private void downloadOpcodeTable() {
		
		try (BufferedInputStream in = new BufferedInputStream(new URL("https://chip8.gulrak.net/data/opcode-table.json").openStream());
				  FileOutputStream fileOutputStream = new FileOutputStream("opcode-table.json")) {
				    byte dataBuffer[] = new byte[1024];
				    int bytesRead;
				    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
				        fileOutputStream.write(dataBuffer, 0, bytesRead);
				    }
				    fileOutputStream.close();
				    in.close();
				} catch (IOException e) {
				    e.printStackTrace();
				}
	}

	public int nibble(char c) {
		if (c >= '0' && c <= '9') return c - '0';
		if (c >= 'a' && c <= 'f') return c - 'a' + 10;
		if (c >= 'A' && c <= 'F') return c - 'A' + 10;
		return -1;
	}


	private String getString(JSONObject jop, String string) {
		String result= null;
		try {
			result = jop.getString(string);
		} catch(Exception e) {
			
		}
		return result;
	}
	
	String decode(int opcode, Dialect dialect, TreeMap<Integer, CC8Label> labels) {
		String result = null;
		for (Chip8Opcode op: mOpcodes) {
			if (op.match(opcode)) {
				switch(dialect) {
					case CHIPPER: result = op.decode(opcode, labels, op.chipper);
						break;
					case OCTO: result = op.decode(opcode, labels, op.octo);
						break;
				}
				break;
			}
		}
		
		return result;
	}

}
