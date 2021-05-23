package Thread;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class getinThread extends Thread{
	CDsell cd;
	private Lock lock;
	private Condition condition;
	public getinThread(CDsell cd,Lock l,Condition c) {
		this.cd=cd;
		this.setDaemon(true);
		lock=l;
		condition=c;
	}
	public void run() {
		while(true) {
			try {
				lock.lock();
				cd.getin();
				condition.signalAll();;
				try {
					condition.await(1000,TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			finally {
				lock.unlock();
			}
		}
	}
}
class saleThread extends Thread{
	CDsell cd;
	static int init=0;
	int id;
	private Lock lock;
	private Condition condition;
	public saleThread(CDsell cd,Lock l,Condition c) {
		this.cd=cd;
		this.setDaemon(true);
		id=++init;
		lock=l;
		condition=c;
	}
	public void run() {
		Random r=new Random();
		while(true) {
			try {
				Thread.sleep(r.nextInt(200));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int buynum=r.nextInt(5);
			buynum=buynum==0?1:buynum;
			int[] kind=new int[buynum];
			int[] singlenum=new int[buynum];
			for(int i=0;i<buynum;i++) {
				kind[i]=r.nextInt(10);
				singlenum[i]=r.nextInt(buynum);
				buynum-=singlenum[i];
			}
			singlenum[0]+=buynum;
			try {
				lock.lock();
				for(int i=0;i<buynum;i++) {
					int k=kind[i];
					int n=singlenum[i];
					if(cd.quantity[k]<n) {
						System.out.println("CD "+k+" stock not enough.");
						if(r.nextBoolean()) {
							condition.signalAll();
							cd.sale(k, n,id,condition);
						}
						else
							System.out.println(new Date()+"SaleThread:"+id+" gives up buying CD "+k);
					}
					else {
						if(n!=0) 
							cd.sale(k, n,id,condition);
					}
				}
			}
			finally {
				lock.unlock();
			}
		}
	}
}
class rentThread extends Thread{
	CDrent cd;
	static int init=0;
	int id;
	private Lock lock;
	private Condition condition;
	public rentThread(CDrent cd,Lock l,Condition c) {
		this.cd=cd;
		this.setDaemon(true);
		id=++init;
		lock=l;
		condition=c;
	}
	public void run() {
		Random r=new Random();
		int r_type=r.nextInt(cd.type)+1;
		while(true) {
			System.out.println(new Date()+"rentThread:"+id+" wants to rent"+"CD "+r_type);
			try {
				lock.lock();
				if(cd.quantity[r_type-1]==0) {
					if(r.nextBoolean()) {
						System.out.println(new Date()+"CD "+r_type+"has been borrowed.");
						try {
							condition.await();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else
						System.out.println(new Date()+"rentThread:"+id+" gives up borrowing");
				}
				else {
					System.out.println(new Date()+"rentThread:"+id+" rents the CD"+r_type);
					cd.quantity[r_type-1]=0;
					try {
						Thread.sleep(r.nextInt(100)+200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					cd.quantity[r_type-1]=1;
					System.out.println(new Date()+"rentThread:"+id+"returns the CD "+r_type);
					condition.signalAll();
				}
				try {
					condition.await(r.nextInt(200), TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
			finally {
				lock.unlock();
			}
		}
	}
}
class controlThread extends Thread{
	CDsell cd1;
	CDrent cd2;
	Lock l=new ReentrantLock();
	Condition c1=l.newCondition();
	Condition c2=l.newCondition();
	public controlThread(CDsell cd1,CDrent cd2) {
		this.cd1=cd1;
		this.cd2=cd2;
	}
	public void run() {
		new getinThread(cd1,l,c2).start();
		new saleThread(cd1,l,c2).start();
		new saleThread(cd1,l,c2).start();
		new saleThread(cd1,l,c2).start();
		new rentThread(cd2,l,c1).start();
		new rentThread(cd2,l,c1).start();
		new rentThread(cd2,l,c1).start();
		try {
			Thread.sleep(120*1000);//程序运行时间
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
public class CDshop{
	public static void main(String[] arg) {
		CDsell cd1=new CDsell();
		CDrent cd2=new CDrent();
		controlThread control=new controlThread(cd1,cd2);
		control.start();
	}
}
