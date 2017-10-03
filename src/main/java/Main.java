import static spark.Spark.*;

import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.parity.Parity;
import org.web3j.protocol.parity.methods.response.PersonalUnlockAccount;
import org.apache.log4j.BasicConfigurator;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.Future;

public class Main {
	
	static Web3j web3;
	static Credentials credentials = null;
	public static _Users_Admin_Desktop_Steps_sol_Steps contract;
	public static final BigInteger GAS_PRICE = BigInteger.valueOf(20000000000L);
	public static final BigInteger GAS_LIMIT = BigInteger.valueOf(4300000);
	
	private final static String localUTC = "./UTC--2017-09-28T18-57-53.555000000Z--79410ded4fd046b723df0b67ae093d14b9635968.json";

    public static void main(String[] args) {
    	BasicConfigurator.configure();
        port(getHerokuAssignedPort());
        get("/hello", (req, res) -> "Hello Heroku World!!!!");
        
        get("/walletID", (request, response) ->{
			System.out.println("UUID: " + request.queryParams("uuid"));
			System.out.println("Creating Wallet");
	       	return createWallet();
	       });
        
        get("/checkWallet", (request, response) ->{
			System.out.println("WalletId: " + request.queryParams("walletId"));
			System.out.println("Checking Wallet");
			boolean found = checkWallet(request.queryParams("walletId"));
	       	return found;
	       });
        
        get("/loadWallet", (request, response) ->{
			System.out.println("Loading Wallet: " + request.queryParams("walletId"));
			
			boolean loaded = loadMyWallet(request.queryParams("walletId"));
	       	return loaded;
	       });
        
        get("/everyoneSteps", (request, response) ->{
			System.out.println("Getting Steps for: " + request.queryParams("date"));
			
			int steps = loadEveryoneSteps(request.queryParams("date"));
	       	return steps;
	       });
        
        get("/saveSteps", (request, response) ->{
			System.out.println("Saving Steps for: " + request.queryParams("uuid"));
			System.out.println("Steps: " + request.queryParams("steps"));
			
			boolean saved = saveSteps(request.queryParams("uuid"),request.queryParams("steps"));
	       	return saved;
	       });
        
        
		   
		   
		getClientVersion();
		
		
		
		//boolean unlocked = unlockMainAccount();
		
		//createWallet();
		
		loadWallet(localUTC);
    }

    static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 4567; //return default port if heroku-port isn't set (i.e. on localhost)
    }
    
    public static boolean saveSteps(String uuid, String steps){
    	
    	return true;
    }
    
    public static boolean loadMyWallet(String walletId){
    
    	boolean gotCred = loadWallet(walletId);
    	
    	if (gotCred){
    		boolean gotContract = createContract();
    		if (gotContract){
    			System.out.println("Got Contract");
    			loadEveryoneSteps("92717");
    			return true;
    		}
    	}
    	
    	return false;
    	
    }
    
    
    public static boolean createContract(){
		contract = _Users_Admin_Desktop_Steps_sol_Steps.load(credentials.getAddress(), web3, credentials, GAS_PRICE, GAS_LIMIT);
		try{
			System.out.println("Contract Valid: " + contract.isValid());
			System.out.println("Address: " + contract.getContractAddress());
			loadEveryoneSteps("92717");
			return true;
		} catch (Exception e){
			System.out.println("Error Creating Contract: " + e.getMessage());
			return false;
		}
		
		
	}
	
	public static int loadEveryoneSteps(String formattedDate){
		Future<Uint256> everyoneSteps = contract.everyoneStepsDate(new Utf8String(formattedDate));
		System.out.println("About to load everyone steps");
	
		try{
			int steps = everyoneSteps.get().getValue().intValue();	
			System.out.println("Steps: " + steps);
			return steps;
		} catch (Exception e){
			System.out.println("Error Loading Everyone Steps: " + e.getMessage());
			
			return 0;
		}
		
	}
	
	public static boolean loadWallet(String jsonUTC){
		try{
			credentials = WalletUtils.loadCredentials(
			        "hellya",
			        "./" + jsonUTC); 
			
			System.out.println("Successfully Loaded Wallet");
			createContract();
			return true;
			
			
		} catch (Exception e){
			System.out.println("Error Loading Wallet: " + e.getMessage());
			
			return false;
		}
	
	}
	
	public static boolean checkWallet(String walletId){
		try{
			credentials = WalletUtils.loadCredentials(
			        "hellya",
			        "./" + walletId);
			
			System.out.println("Wallet Exists");
			return true;
		} catch (Exception e){
			System.out.println("Error Loading Wallet: " + e.getMessage());
			return false;
		}
	
	}
	
	public static String createWallet(){
		
		String fileName = "not set";
		try{
			fileName = WalletUtils.generateNewWalletFile(
			        "hellya",
			        new File("./"),false);	
		} catch (Exception e){
			System.out.println("Error Creating Wallet: " + e.getMessage());
		}
		
		System.out.println("Created Wallet: " + fileName);
		return fileName;
		

	}
	
	public static boolean unlockMainAccount(){
		Parity parity = Parity.build(new HttpService("http://45.55.4.74:8545"));  // defaults to http://localhost:8545/
		
		PersonalUnlockAccount personalUnlockAccount = null;
		try {
			personalUnlockAccount = parity.personalUnlockAccount("0x4d5bcceba61400e52809a9e29eaccce328b4b43f", "hellya").sendAsync().get();	
		} catch (Exception e){
			System.out.println("Error: " + e.getMessage());
		}
        

        if (personalUnlockAccount.accountUnlocked()) {
        	System.out.println("Main Account Unlocked with Parity");
            return true;
        } else {
        	System.out.println("Error Unlocking Main Account with Parity");

            return false;
        }		
	}
	
	public static void getClientVersion(){

		web3 = Web3j.build(new HttpService("http://45.55.4.74:8545"));
		Web3ClientVersion web3ClientVersion = null;
		
		try{
			web3ClientVersion = web3.web3ClientVersion().sendAsync().get();	
		} catch (Exception e){
			System.out.println("Error: " + e.getMessage());
		}
		
		System.out.println(web3ClientVersion.getWeb3ClientVersion());
	}

}