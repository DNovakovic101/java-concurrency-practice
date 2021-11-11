package trinaestiprvi;

import java.util.concurrent.Semaphore;

import os.simulation.Application;
import os.simulation.AutoCreate;
import os.simulation.Container;
import os.simulation.Item;
import os.simulation.Operation;
import os.simulation.Thread;

/*
 * U okviru maturske ekskurzije, za djake iz tri evropske drzave - Engleske,
 * Nemacke i Italije - je organizovan obilazak muzeja Louvre u Parizu. Sve tri
 * grupe djaka borave neko vreme ispred muzeja, nakon cega ulaze u muzej i uzi-
 * vaju u izlozenim umetnickim delima. Medjutim, u jednom momentu samo djaci
 * jedne drzave mogu boraviti u muzeju, jer bi se u suprotnom njihovi vodici
 * morali nadvikivati i niko nista ne bi cuo.
 * 
 * Sinhronizovati boravak djaka u muzeju koriscenjem semafora, tako da u jednom
 * momentu samo jedna grupa bude unutar muzeja. Svaki djak je predstavljen jed-
 * nom niti cija klasa odredjuje drzavu iz koje on dolazi.
 */
public class MuzejSemafori extends Application{
	
	private class Vodic{
		
		private Semaphore muzej;
		private Semaphore mutex = new Semaphore(1);
		private int brStud = 0 ;
		
		public Vodic(Semaphore muzej) {
			this.muzej = muzej;
		}
		
		private void zapocniObilazak() throws InterruptedException {
			mutex.acquire();
			try {
				
				brStud++;
				if(brStud==1) {
					try {
						muzej.acquire();
					}catch(InterruptedException e) {
						brStud--;
						throw e;
					}
				}		
			}finally {
				mutex.release();
			}
		}
		private void zavrsiObilazak() {
			mutex.acquireUninterruptibly();
			try {
				
				brStud--;
				if(brStud==0) {
						muzej.release();
					}						
			}finally {
				mutex.release();
			
		}
	}
	}
	private Semaphore muzejSemafor = new Semaphore(1);
	
	private Vodic engleskiVodic = new Vodic(muzejSemafor);
	private Vodic nemackiVodic = new Vodic(muzejSemafor);
	private Vodic italijanskiVodic = new Vodic(muzejSemafor);
	
	@AutoCreate(8)
	protected class Englez extends Thread {

		@Override
		protected void step() {
			odmara();
			try {
				engleskiVodic.zapocniObilazak();
				try {
				obilazi();
				}finally {
					engleskiVodic.zavrsiObilazak();
					}
			} catch (InterruptedException e) {
					stopGracefully();
			}
			}
			
		}
	

	@AutoCreate(8)
	protected class Nemac extends Thread {

		@Override
		protected void step() {
			odmara();
			try {
				nemackiVodic.zapocniObilazak();
				try {
				obilazi();
				}finally {
					nemackiVodic.zavrsiObilazak();
					}
			} catch (InterruptedException e) {
					stopGracefully();
			}
			}
	}

	@AutoCreate(8)
	protected class Italijan extends Thread {

		@Override
		protected void step() {
			odmara();
			try {
				italijanskiVodic.zapocniObilazak();
				try {
				obilazi();
				}finally {
					italijanskiVodic.zavrsiObilazak();
					}
			} catch (InterruptedException e) {
					stopGracefully();
			}
			}
	}

	// ------------------- //
	//    Sistemski deo    //
	// ------------------- //
	// Ne dirati kod ispod //
	// ------------------- //

	protected final Container englezi   = box("Енглези").color(MAROON);
	protected final Container nemci     = box("Немци").color(ROYAL);
	protected final Container italijani = box("Италијани").color(ARMY);
	protected final Container muzej     = box("Музеј").color(NAVY);
	protected final Container main      = column(row(englezi, nemci, italijani), muzej);
	protected final Operation englez    = init().container(englezi).name("Енглез %d").color(RED);
	protected final Operation nemac     = init().container(nemci).name("Немац %d").color(PURPLE);
	protected final Operation italijan  = init().container(italijani).name("Италијан %d").color(GREEN);

	protected final Operation odmaranje = duration("7±2").text("Одмара").textAfter("Чека");
	protected final Operation obilazak  = duration("5±2").text("Обилази").container(muzej).textAfter("Обишао").update(this::azuriraj);

	protected void odmara() {
		odmaranje.performUninterruptibly();
	}

	protected void obilazi() {
		obilazak.performUninterruptibly();
	}

	protected void azuriraj(Item item) {
		long brE = muzej.stream(Englez.class).count();
		long brN = muzej.stream(Nemac.class).count();
		long brI = muzej.stream(Italijan.class).count();
		muzej.setText(String.format("%d / %d / %d", brE, brN, brI));
		if (brE == 0 && brN == 0 && brI == 0) {
			muzej.setColor(NAVY);
		} else if (brE > 0 && brN == 0 && brI == 0) {
			muzej.setColor(MAROON);
		} else if (brE == 0 && brN > 0 && brI == 0) {
			muzej.setColor(ROYAL);
		} else if (brE == 0 && brN == 0 && brI > 0) {
			muzej.setColor(ARMY);
		} else {
			muzej.setColor(CARBON);
		}
	}

	@Override
	protected void initialize() {
		azuriraj(null);
	}

	public static void main(String[] a) {
		launch("Музеј");
	}
}
