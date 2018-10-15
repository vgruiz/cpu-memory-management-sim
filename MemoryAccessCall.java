

public class MemoryAccessCall {
	int call;		//0 for read, 1 for write
	String address;
	int value;		//if the call is 0, this will be 0
	
	public MemoryAccessCall(int c, String a, int v) {
		call = c;
		address = a;
		value = v;
	}
}