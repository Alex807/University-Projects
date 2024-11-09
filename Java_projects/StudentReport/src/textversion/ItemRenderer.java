package textversion;

public interface ItemRenderer<Type> { 
	public String getGroupByKey(Type item);
	public String buildeDisplayText(Type item);
}
