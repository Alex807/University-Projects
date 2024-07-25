package zippingStack;

import java.io.File;

public class StackItem { 
	
	private final File item; 
	private final String parentItemPath;
	
	public StackItem(File item, String parentItemPath) {
		this.item = item; 
		this.parentItemPath = parentItemPath;
	}

	public File getItem() {
		return item;
	}

	public String getParentItemPath() {
		return parentItemPath;
	}
}
