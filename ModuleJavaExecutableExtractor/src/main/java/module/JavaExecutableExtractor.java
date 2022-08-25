package module;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import toolkit.core.api.module.capabilities.RunnableModule;
import toolkit.core.api.module.capabilities.StoppableModule;
import utils.StringUtils;

public class JavaExecutableExtractor implements RunnableModule, StoppableModule {
	
	private final Clipboard    clipboard;
	private final String       simpleClassToken    = "class ";
	private final int          simpleClassTokenLen = simpleClassToken.length();
	private final List<String> classTokens         = Arrays.asList(new String[] {"public class ", simpleClassToken, "abstract class ", "final class "});
	private final int          classTokensLen      = classTokens.size();
	private final String       packageTokens       = "package ";
	private final int          packageTokensLen    = packageTokens.length();
	
	private boolean pause;
	
	public static void main(String[] args) {
		new JavaExecutableExtractor().runAction();
	}
	
	public JavaExecutableExtractor() {
		clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		pause     = false;
	}
	
	public String getIdentifier() {
		return "JavaExecutableExtractor v1.0.0";
	}

	public String getDescription() {
		return "Inspects and alters the contents of the clipboard. Detect a java class and replace it with its URI.";
	}

	public void runAction() {
		while (!pause) {
			try {
				sleep(100);
				Transferable tr = clipboard.getContents(null);
				sleep(100);
				if (!tr.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					sleep(1000);
					continue;
				}
				String strData = (String)tr.getTransferData(DataFlavor.stringFlavor);
				
				if (!StringUtils.isNullOrEmptyOrWhiteSpaces(strData)) {
					List<String> lines = Arrays.asList(strData.split("\\r?\\n"));
					
					if (lines.size() == 0) {
						sleep(1000);
						continue;
					}
					
					if (lines.size() == 1) {
						nameCase(lines);
						continue;
					}
					
					classCase(lines);
					
				}
				

			} catch (Exception e) {
				e.printStackTrace();
			}
			
			sleep(500);
		}		
	}

	private void nameCase(List<String> lines) {
		String line = lines.get(0);
		
		if (!startsWithClassToken(line)) {
			sleep(1000);
			return;
		}
		
		String className = getClassName(line);
		String upperCase = toUpperCaseWithSeparator(className, '_');
		
		if (!StringUtils.isNullOrEmptyOrWhiteSpaces(upperCase)) {
			setClipboardString(upperCase);
			System.out.println(StringUtils.concat(getIdentifier(), ": ", upperCase));
		}
	}
	
	private String toUpperCaseWithSeparator(String className, char sep) {
		if (className == null) {
			return "";
		}
		
		if (className.length() < 2) {
			return className;
		}
		
		List<Integer> upperChange = new LinkedList<>();
		int i = 0;
		int max = className.length();
		boolean lastUpper = Character.isUpperCase(className.charAt(i));
		i++;
		while (i<max) {
			boolean thisUpper = Character.isUpperCase(className.charAt(i));
			if (thisUpper && !lastUpper) {
				upperChange.add(i);
			}
			
			lastUpper = thisUpper;
			i++;
		}
		
		char[] chars = new char[className.length()+upperChange.size()];
		int pos = 0;
		Iterator<Integer> iter = upperChange.iterator();
		int index = iter.hasNext() ? iter.next() : -1;
		int offset = 0;
		for (int j = 0; j < max; j++) {
			if(pos == index + offset) {
				chars[pos++] = sep;
				index = iter.hasNext() ? iter.next() : -1;
				offset++;
			}
			chars[pos++]=Character.toUpperCase(className.charAt(j));
		}
		
		return new String(chars);
	}

	private void classCase(List<String> lines) {
		Optional<String> optStr = lines.stream()
									   .filter(StringUtils::isNotNullOrEmptyOrWhiteSpaces)
									   .filter(item -> item.startsWith(packageTokens))
									   .findAny();

		if (!optStr.isPresent()) {
			sleep(1000);
			return;
		}
		
		Optional<String> optStr2 = lines.stream()
							.filter(StringUtils::isNotNullOrEmptyOrWhiteSpaces)
							.filter(item -> startsWithClassToken(item))
							.findAny();
		
		if (!optStr2.isPresent()) {
			sleep(1000);
			return;
		}
		
		String packLine = optStr.get(); //Excepcion por no tener valor
		String packageURI = packLine.substring(packageTokensLen, packLine.length()-1);
		
		String classLine = optStr2.get();
		String className = getClassName(classLine);
		
		if (!StringUtils.isNullOrEmptyOrWhiteSpaces(packageURI) && !StringUtils.isNullOrEmptyOrWhiteSpaces(className)) {
			String classURI = StringUtils.concat(packageURI, ".", className);
			System.out.println(StringUtils.concat(getIdentifier(), ": ", classURI));
			setClipboardString(classURI);
		}
	}

	private boolean startsWithClassToken(String item) {
		boolean found = false;
		int i = 0;
		
		while (i < classTokensLen && !found) {
			if (item.startsWith(classTokens.get(i))) {
				found = true;
			}
			i++;
		}
		
		return found;
	}

	private void sleep(int milis) {
		try {
			Thread.sleep(milis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private DataFlavor[] cacheDataFlavor = new DataFlavor[] {java.awt.datatransfer.DataFlavor.stringFlavor};
	private void setClipboardString(String txt) {
		clipboard.setContents(new Transferable() {
			@Override
			public final boolean isDataFlavorSupported(DataFlavor flavor) {
				return flavor == java.awt.datatransfer.DataFlavor.stringFlavor;
			}
			
			@Override
			public final DataFlavor[] getTransferDataFlavors() {
				return cacheDataFlavor;
			}
			
			@Override
			public final Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
				if (isDataFlavorSupported(flavor)) {
					return txt;					
				} else {
					return "";
				}
			}
		}, null);
	}

	private String getClassName(String classLine) {
		int index = classLine.indexOf(simpleClassToken);
		
		if (index < 0) {
			return "";
		}
		
		index += simpleClassTokenLen;
		
		int untilSpace = index;
		int classLineLen = classLine.length();
		while (untilSpace < classLineLen) {
			if (classLine.charAt(untilSpace) == ' ') {
				break;
			}
			
			untilSpace++;
		}
		
		return classLine.substring(index, untilSpace);
	}


	public void requestStop(boolean closingModule) {
		pause = true;
	}

	public boolean isStopRequested() {
		return pause;
	}

}
