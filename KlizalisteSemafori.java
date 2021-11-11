package trinaestiprvi;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import os.simulation.Application;
import os.simulation.AutoCreate;
import os.simulation.Container;
import os.simulation.Item;
import os.simulation.Operation;
import os.simulation.Thread;
import trinaestiprvi.KlizalisteAtomici.Pristup;

/*
 * Data je simulacija rada Ledene šume u Dunavskom parku. Korisnici mogu dola-
 * ziti bilo kada u toku radnog vremena i koristiti usluge klizališta. Prilikom
 * ulaska korišćenje klizaljki je obavezno.
 * 
 * Poznavajući kućni red, korisnici prilikom dolaska prvo iznajmljuju klizaljke,
 * obuvaju ih i tek potom stupaju na stazu za klizanje. Takođe, prilikom odla-
 * ska korisnici obuvaju svoju obuću, vraćaju klizaljke i tek potom odlaze.
 * 
 * Ledena šuma se skoro otvorila i prve nedelje je velika gužva jer je većina
 * mladih parova u gradu odlučila da se malo oproba na ledu. Svi parovi se sa-
 * staju ispred ulaza na klizalište.
 * 
 * Pomoću klasa iz paketa java.util.concurrent.atomic, sinhronizovati ove mlade
 * parove tako da momak neće iznajmiti klizaljke i početi da kliza bez da sače-
 * ka devojku, niti će se obuti i otići bez devojke. Analogno ni devojka neće
 * uzeti klizaljke i klizati, ili otići iz dvorane bez da i momak uradi to isto.
 * 
 * Dodatno 1
 * 
 * Toplo vreme je učinilo da led na klizalištu ne bude dovoljno debeo i čvrst.
 * Ovakav led može da izdrži najviše 300 kilograma težine. Sinhronizovati kli-
 * zače tako da ne stupaju na klizalište ako bi pri tome pukao led.
 * 
 * Uključiti animaciju kršenja uslova postavljanjem polja MAX_TEZINA na 300.
 * 
 * Dodatno 2
 * 
 * Umesto osnovnog problema, sinhronizovati klizače tako da dok klizaju momci,
 * devojke ne stupaju na klizalište. Takođe, dok devojke klizaju, momci ne stu-
 * paju na klizalište.
 * 
 * Uključiti animaciju kršenja uslova postavljanjem polja JEDAN_POL na true.
 * 
 * Dodatno 3
 *
 * Zbog velike gužve, klizalište ima samo 10 parova klizaljki na raspolaganju
 * za iznajmljivanje.
 * 
 * Uključiti animaciju kršenja uslova postavljanjem polja MAX_BR na 10.
 * 
 * Dodatno 4
 * 
 * Zbog velike gužve, klizalište ima ograničen broj klizaljki, tj. po 2 para od
 * sledećih veličina: 28‐31, 32‐35, 36‐39, 40‐43, 44‐47, 48‐51 i 52‐55.
 *
 * Uključiti animaciju kršenja uslova postavljanjem polja MAX_CIPELA na 2.
 * 
 * Dodatno 5
 * 
 * Rešiti dodatne probleme 1, 2 i 4 zajedno.
 * 
 * Dodatno 6
 * 
 * Rešiti osnovni i dodatni problem 1 ako su tezine tipa double.
 * 
 * Promeniti polje tezina na: private double tezina = randomDouble(...);
 * 
 * Dodatno 7
 * 
 * Rešiti osnovni tako da umesto u parovima, klizaci klizaju u grupama od tri:
 * jedan momak i dve devojke ili jedna devojka i dva momka. Takodje u takvim
 * grupama i odlaze sa klizalista.
 */
public class KlizalisteSemafori extends Application{
	private class Pristup{
		
		private void sacekajMomka() {
			
		}
		private void sacekajDevojku() {
			
		}
	}
	private Pristup pristup = new Pristup();
	@AutoCreate
	protected class Momak extends Thread {

		private int brojCipela = randomInt(38, 56);
		private int tezina = randomInt(70, 100);

		@Override
		protected void run() {
			obuvaSe();
			kliza();
			izuvaSe();
		}
	}

	@AutoCreate
	protected class Devojka extends Thread {

		private int brojCipela = randomInt(28, 46);
		private int tezina = randomInt(60, 80);

		@Override
		protected void run() {
			obuvaSe();
			kliza();
			izuvaSe();
		}
	}

	protected final int MAX_TEZINA = 0;
	protected final int MAX_BR = 0;
	protected final int MAX_CIPELA = 0;
	protected final boolean JEDAN_POL = false;

	// ------------------- //
	//    Sistemski deo    //
	// ------------------- //
	// Ne dirati kod ispod //
	// ------------------- //

	protected final Container ulaz      = box("Улаз").color(NAVY);
	protected final Container garderoba = box("Клизаљке").color(OLIVE);
	protected final Container izlaz     = box("Клизаљке").color(OLIVE);
	protected final Container dvorana   = box("Ледена дворана");
	protected final Container main      = column(row(ulaz, garderoba, izlaz), dvorana);
	protected final Operation momak     = duration("2±1").container(ulaz).name("Момак %d").text("Чека").color(AZURE);
	protected final Operation devojka   = duration("2±1").container(ulaz).name("Девојка %d").text("Чека").color(ROSE);
	protected final Operation obuvanje  = duration("4").text("Узима").container(garderoba).containerAfter(garderoba);
	protected final Operation klizanje  = duration("6±2").text("Клиза").container(dvorana).containerAfter(dvorana).updateBefore(this::azuriraj);
	protected final Operation izuvanje  = duration("4").text("Враћа").container(izlaz).containerAfter(izlaz).updateBefore(this::azuriraj);

	protected void obuvaSe() {
		obuvanje.performUninterruptibly();
	}

	protected void kliza() {
		klizanje.performUninterruptibly();
	}

	protected void izuvaSe() {
		izuvanje.performUninterruptibly();
	}

	protected void azuriraj(Item item) {
		int br = 0;
		int brM = 0;
		int brD = 0;
		double tezina = 0.0;
		List<Integer> cipele = new ArrayList<>();
		Map<Integer, Integer> grupe = new HashMap<>();
		for (Momak momak : dvorana.getItems(Momak.class)) {
			br += 1;
			brM += 1;
			tezina += momak.tezina;
			cipele.add(momak.brojCipela);
			grupe.compute(momak.brojCipela / 4, (k, v) -> v == null ? 1 : v + 1);
		}
		for (Devojka devojka : dvorana.getItems(Devojka.class)) {
			br += 1;
			brD += 1;
			tezina += devojka.tezina;
			cipele.add(devojka.brojCipela);
			grupe.compute(devojka.brojCipela / 4, (k, v) -> v == null ? 1 : v + 1);
		}
		Collections.sort(cipele);
		int max = grupe.values().stream().mapToInt(Integer::intValue).max().orElse(0);
		boolean tezinaOk = MAX_TEZINA == 0 || tezina <= MAX_TEZINA;
		boolean brOk = MAX_BR == 0 || br <= MAX_BR;
		boolean cipeleOk = MAX_CIPELA == 0 || max <= MAX_CIPELA;
		boolean polOk = !JEDAN_POL || brM == 0 || brD == 0;
		StringBuilder builder = new StringBuilder();
		if (MAX_BR != 0) {
			builder.append(String.format("%d", br));
		}
		if (MAX_CIPELA != 0) {
			if (builder.length() != 0) {
				builder.append("  ");
			}
			builder.append(String.format("%s", cipele));
		}
		if (MAX_TEZINA != 0) {
			if (builder.length() != 0) {
				builder.append("  ");
			}
			builder.append(String.format("%4.2f kg", tezina));
		}
		dvorana.setText(builder.toString());
		if (tezinaOk && brOk && cipeleOk && polOk) {
			dvorana.setColor(SILVER);
		} else {
			dvorana.setColor(MAROON);
		}
	}

	@Override
	protected void initialize() {
		azuriraj(null);
	}

	public static void main(String[] arguments) {
		launch("Клизалиште");
	}
}
