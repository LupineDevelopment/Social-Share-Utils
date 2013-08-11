package cc.lupine.quicksocial.shareutils;

public abstract class SharingAdapter {
	
	public abstract void setOnShareListener(OnShare listener);

	public abstract void shareAsync();
	
}
