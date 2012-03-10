package sat.repl;

import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;

public abstract class REPL implements Runnable {
	protected Scanner in;
	protected PrintStream out;
	
	private String prompt;
	private String prompt_default;
	
	private HashMap<String, Method> api = new HashMap<String, Method>();
	
	public REPL(InputStream in, PrintStream out, String prompt) {
		this.in = new Scanner(in);
		this.out = out;
		
		this.prompt = this.prompt_default = prompt;
		
		Method[] methods = this.getClass().getDeclaredMethods();
		
		for(Method method : methods) {
			api.put(method.getName(), method);
		}
	}
	
	public REPL(InputStream in, PrintStream out) {
		this(in, out, "> ");
	}

	public void run() {
		while(true) {
			out.print(prompt);
			
			if(!in.hasNextLine()) break;
			
			String line = in.nextLine();
			eval(line);
		}
		
		out.println("");
	}
	
	public Thread runInNewThread() {
		Thread thread = new Thread(this);
		thread.start();
		return thread;
	}
	
	private String[] split(String line) {
		Vector<String> parts = new Vector<String>();
		StringBuffer buffer = new StringBuffer();
		
		boolean string_mode = false;
		boolean escape_mode = false;
		
		for(int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			
			if(escape_mode) {
				buffer.append(c);
				escape_mode = false;
				continue;
			}
			
			switch(c) {
				case '\\':
					escape_mode = true;
					break;
					
				case '"':
					string_mode = !string_mode;
					break;
					
				case ' ':
				case '\t':
					if(!string_mode) {
						if(buffer.length() > 0) {
							parts.add(buffer.toString());
							buffer.setLength(0);
						}
						
						break;
					}
					
				default:
					buffer.append(c);
			}
		}
		
		if(buffer.length() > 0) {
			parts.add(buffer.toString());
		}
		
		return parts.toArray(new String[parts.size()]);
	}
	
	private void eval(String line) {
		String[] parts = split(line);
		
		if(parts.length == 0) {
			// Input is empty
			return;
		}
		
		int args_counts = parts.length-1; // Ignore command
		
		String cmd = parts[0];
		
		if(!api.containsKey(cmd)) {
			out.println("Unknow command: " + cmd);
			return;
		}
		
		Method method = api.get(cmd);
		
		int args_required = method.getParameterTypes().length;
		
		String[] args = new String[args_required];
		for(int i = 0; i < args_required; i++) {
			if(i >= args_counts) {
				args[i] = "";
			} else {
				args[i] = parts[i+1];
			}
		}
		
		try {
			method.invoke(this, (Object[]) args);
		} catch(Exception e) {
			e.printStackTrace(out);
		}
	}
	
	public String prompt() {
		return prompt;
	}
	
	public void setPrompt(String newPrompt) {
		prompt = newPrompt;
	}
	
	public void restorePrompt(String newPrompt) {
		prompt = prompt_default;
	}
}
