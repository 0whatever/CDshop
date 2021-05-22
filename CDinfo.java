package Thread;

import java.util.Date;

class CDsell {
	int type;
	int[] quantity;
	public CDsell() {
		type=10;
		quantity=new int[10];//0→租借
	}
	//每次进货都是补齐
	void getin() {
		for(int i=0;i<type;i++)
			quantity[i]=10;
		System.out.println(new Date()+"Get in!");
	}
	//商品种类0-9
	void sale(int kind,int n,int id) {
		while(quantity[kind]<n) {
			notifyAll();
			try {
				System.out.println(new Date()+"!Waiting..."+id+":→"+n+" CD "+kind);
				this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		quantity[kind]-=n;
		System.out.println(new Date()+"SaleThread:"+id+" gets "+n+" CD "+kind+"(CDshop sales CD "+kind+" "+n+")");
	}
}
class CDrent{
	int type;
	int[] quantity;
	public CDrent(){
		type=10;
		quantity=new int[10];
		for(int i=0;i<type;i++)
			quantity[i]=1;
	}
}
