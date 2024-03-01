/*Clasă de bază: ContBancar

Atribute: numarCont (int), sold (double), detinator (String).
Metode:
depuneBani(double suma): Adaugă o sumă la soldul contului.
extrageBani(double suma): Extrage o sumă din cont dacă fondurile permit.
transferaBani(ContBancar destinatar, double suma): Transferă o sumă către un alt cont bancar.
afiseazaSold(): Afișează soldul curent al contului.
afiseazaDetalii(): Afișează detalii despre cont (număr, detinator, sold).
Clasă derivată: ContEconomii

Atribute suplimentare: rataDobanzii (double), perioadaDepozitare (int).
Metode:
adaugaDobanda(): Adaugă dobânda la sold în funcție de rata și perioada de depozitare.
Suprascrie metoda afiseazaDetalii() pentru a include informații specifice contului de economii.
Clasă derivată: ContCurent

Atribute suplimentare: limitaDeOverdraft (double).
Metode:
depasesteLimita(double suma): Verifică dacă o sumă depășește limita de overdraft.
Suprascrie metoda extrageBani(double suma) pentru a gestiona situațiile de overdraft.
Clasă derivată: ContInvestitii

Atribute suplimentare: portofoliuInvestitii (ArrayList<String>), comisionTranzactie (double).
Metode:
adaugaInvestitie(String numeInvestitie): Adaugă o investiție la portofoliu.
efectueazaTranzactie(double suma): Efectuează o tranzacție asupra portofoliului și aplică comisionul.
Suprascrie metoda afiseazaDetalii() pentru a include informații specifice contului de investiții.
Clasă: Banca

Atribut: listaConturi (ArrayList<ContBancar>).
Metode:
adaugaCont(ContBancar cont): Adaugă un cont în lista băncii.
stergeCont(ContBancar cont): Șterge un cont din lista băncii.
proceseazaDobanzi(): Aplică dobânda asupra tuturor conturilor de economii.
efectueazaTransfer(ContBancar sursa, ContBancar destinatie, double suma): Transferă bani între două conturi.
afiseazaRaport(): Afișează un raport cu detalii despre toate conturile din bancă.
Testare:

În funcția main, creați diverse tipuri de conturi și adăugați-le într-o instanță a clasei Banca.
Efectuați diverse operațiuni (depunere, extragere, transfer, adăugare investiții) pentru a testa funcționalitățile implementate.
Apelați metoda afiseazaRaport() pentru a confirma corectitudinea implementării.*/

import java.util.ArrayList;  //import the  ArrayList class

interface BankAccountType{ 
	
	public void depositMoney(double amountOfMoney);
	
	public void withdrawMoney(double amountOfMoney);
	
	public void transferMoney(BankAccountType recipientBankAccount, double amountOfMoney);
	
	public double getAccountBalance();
	
	public void setAccountBalance(double updatedBalance);
	
	public String showDetails(); 
}

abstract class AbstractBankAccount implements BankAccountType{
	private int accountNumber;
	private String accountHolderName;
	private double accountBalance; 	
	private double maximumWithdrawalAmount = 2500.5; 
	
	protected AbstractBankAccount(String accountHolderName, int accountNumber, double accountBalance){ 
		this.accountHolderName= accountHolderName;
		this.accountNumber = accountNumber; 
		this.accountBalance = accountBalance;
	}
	
	public void depositMoney(double amountOfMoney){ 
		if (amountOfMoney >= 0){ 
			accountBalance += amountOfMoney;		
		}else{ 
			System.out.println("Nu se pot depune sume NEGATIVE !\n");
		}
	}
	
	public void withdrawMoney(double amountOfMoney){ 
		if (amountOfMoney <= maximumWithdrawalAmount){ 
			if (amountOfMoney <= accountBalance){ 
				accountBalance -= amountOfMoney;
			}else{ 
				System.out.println("Retragere nerealizata, fonduri insuficiente!\n");
			}
		}else{
			System.out.println("Limita de retragere depasita!\n");
		}
	}
	
	public void transferMoney(BankAccountType recipientBankAccount, double amountOfMoney){ 
		if (amountOfMoney <= accountBalance){ 
			recipientBankAccount.depositMoney(amountOfMoney); 
			this.withdrawMoney(amountOfMoney);
		}else {
			System.out.print("Transferul nu a putut fi realizat!\n");
		}
	}
	
	public void setAccountBalance(double updatedBalance){ 
		accountBalance = updatedBalance;
	}
	
	public double getAccountBalance(){ 
		return accountBalance;
	}
	
	public String showDetails(){ 
		return "Nume detinator: " + accountHolderName + " | Numar_cont: " + accountNumber + " | Sold: " + accountBalance + "$\n";
	}
	
}

class SavingsAccount extends AbstractBankAccount{ 
	private double interestOnDeposit = 0;  //value is in percentages
	
	public SavingsAccount(String accountHolderName, int accountNumber, double accountBalance){ 
		super(accountHolderName, accountNumber, accountBalance);
	}
	
	public void depositMoney(double amountOfMoney){ 
		if (amountOfMoney >= 0){ 
			double interestAssNumber = interestOnDeposit/100;
			amountOfMoney += (amountOfMoney * interestAssNumber);  //we apply interest on deposit value
			
			super.depositMoney(amountOfMoney);
		}else{ 
			System.out.println("Nu se pot depune sume NEGATIVE !\n");
		}
	}
	
	public String showDetails(){ 
		return "Tip cont: ECONOMII | " + super.showDetails();
	}
}

class CurrentAccount extends AbstractBankAccount{
	//fac alt array list cu o lista de abonamente(nume, si suma lunara);
	public CurrentAccount(String accountHolderName, int accountNumber, double accountBalance){ 
		super(accountHolderName, accountNumber, accountBalance);
	}
	
	public void addMoneyFromOtherAccount(BankAccountType sendingAccount, double amountOfMoney){ 
		sendingAccount.transferMoney(this, amountOfMoney);
	}
	//alta metoda in care achit toate abonamentele
	public String showDetails(){ 
		return "Tip cont: CURENT | " + super.showDetails();
	}
}

class InvestmentsAccount extends AbstractBankAccount{ 
	private double transactionFee = 1; 
	private ArrayList<String> investmentsPortofolio; 
	
	public InvestmentsAccount(String accountHolderName, int accountNumber, double accountBalance){ 
		super(accountHolderName, accountNumber, accountBalance); 
		investmentsPortofolio = new ArrayList<String>();
	}
	
	public void addInvestment(String investmentName){ 
		investmentsPortofolio.add(investmentName);  //add investment in our ArrayList
	}
	
	public void makeNewTransaction(double amountOfMoney){ 
		double accountBalance = super.getAccountBalance();
		
		if (accountBalance < (amountOfMoney + transactionFee)){ 
			System.out.print("Tranzactia NU a putut fi efectuata! Fonduri insuficiente!\n"); 
			return;
		}else {
			accountBalance -= (amountOfMoney + transactionFee);  
			setAccountBalance(accountBalance);
		}
	}
	
	public void removeInvestment(String investmentName){ 
		for (int i=0; i < investmentsPortofolio.size(); i++){ 
			String currentInvestment = investmentsPortofolio.get(i);
			if (currentInvestment.equals(investmentName)){ //if 'true' investment was finded
				investmentsPortofolio.remove(i);
			}
		}
	}
	
	public String showDetails(){ 
		String result = super.showDetails() + "\n Portofoliu_Investitii: "; 
		for (int i=0; i < investmentsPortofolio.size(); i++){ 
			result += investmentsPortofolio.get(i) + "  ";
		}
		return result;
	}
}

public class Main{ 
	public static void main(String args[]){ 
	//Nume, Numar_cont, Balanta
		BankAccountType cont1 = new CurrentAccount("Alex", 12345, 500); 
		BankAccountType cont2 = new SavingsAccount("Radu", 12346, 700); 
		
		cont1.depositMoney(203.4); 
		cont1.transferMoney(cont2, 203.5);
		
		System.out.print(cont1.showDetails()); 
		System.out.print(cont2.showDetails());
		
	}
}
