package com.pdc.sol;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.EthChainId;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Convert;
import java.util.List;




/**
 * Hello world!
 *
 */
import lombok.extern.slf4j.Slf4j;



@Slf4j
public class App {

    public static Web3j web3j;
    public static int chainId = 8801;

    // 钱包文件保持路径，请替换位自己的某文件夹路径
    public static String walletFilePath = "./wallet";

    // 钱包文件名
    public static String walletFileName = "wallet.json";
    public static String password = "123456";

    public static String walletAddress = "0x4837e39138ef40704d2a97b015828d006e018fd6";

    //kovan 0xD8d2cF6996415dCbA49b270420d8AA195d0dA7C7
    //violin 0xD996c4BD6bd52f1255B50255D184Bb1c1360C982
    //0x76A232FDce2ddb24d41CA5Ba8E3C285bDe8793E5
    public static String contractAddress = "0x76A232FDce2ddb24d41CA5Ba8E3C285bDe8793E5";
    public static String mnemonic = "";
    //public static String rpc = "http://47.243.254.231/rpc";
    public static String rpc = "http://124.251.110.238/rpc";

    public static Credentials credentials;

    public static PDCERC721IpfsManager contract;

    public static void main( String[] args ) throws Exception
    {
        init(args);
        web3j = Web3j.build(new HttpService(rpc));
        getBlance(walletAddress);
        //creatAccount(password);
        //creatBip39Account(password);
        //creatBip39AccountFromMnemonic(password);
        loadWallet(walletFileName, password); 
        chain_info();
        contractAddress(contractAddress);
        showAccountTokens(walletAddress);
        mint_token();
        showTokens();
        //transto();
    }

    public static void init(String[] args) throws Exception {
        log.info("args[kovan/violin]: {}", args);
        String chain = "violin";
        if (args.length == 1) {
            chain = args[0];
        }

        if (chain.equals("kovan")) {
            contractAddress = "0xD8d2cF6996415dCbA49b270420d8AA195d0dA7C7";
            rpc = "https://kovan.infura.io/v3/e1ac6790237a4044bff3b676bae7e257";
        } 
       
        log.info("rpc: {}", rpc);
        log.info("contract address: {}", contractAddress);
    }
    public static void chain_info() throws Exception {
        BigInteger gasPrice = web3j.ethGasPrice().sendAsync().get().getGasPrice();
        String clientVersion = web3j.web3ClientVersion().send().getWeb3ClientVersion();
        log.info("version: {}", web3j.netVersion().send().getNetVersion());
        log.info("client version: {}", clientVersion);
        log.info( "version end!" );

        EthChainId chainId = web3j.ethChainId().send();
        log.info("chainId: {}", chainId.getChainId().longValue());
    }

    public static void getBlance(String walletAddress) throws IOException {
        // 第二个参数：区块的参数，建议选最新区块
        EthGetBalance balance = web3j.ethGetBalance(walletAddress, DefaultBlockParameter.valueOf("latest")).send();
        // 格式转化 wei-ether
        String blanceETH = Convert.fromWei(balance.getBalance().toString(), Convert.Unit.GWEI).toPlainString()
                .concat(" ether");
        log.info("blanceETH:{}", blanceETH);
    }

    // 创建一个钱包，并生成json文件
    public static void creatAccount(String password) throws NoSuchAlgorithmException, NoSuchProviderException,
            InvalidAlgorithmParameterException, CipherException, IOException {
        walletFileName = WalletUtils.generateNewWalletFile(password, new File(walletFilePath), false);
        log.info("walletName: {}", walletFileName);
    }

    // 创建一个钱包，并生成json文件
    public static void creatBip39Account(String password) throws NoSuchAlgorithmException, NoSuchProviderException,
            InvalidAlgorithmParameterException, CipherException, IOException {
        WalletUtils.generateBip39Wallet(password, new File(walletFilePath));
    }

    // 创建一个钱包，并生成json文件
    public static void creatBip39AccountFromMnemonic(String password) throws NoSuchAlgorithmException, NoSuchProviderException,
            InvalidAlgorithmParameterException, CipherException, IOException {
        WalletUtils.generateBip39WalletFromMnemonic("", mnemonic, new File(walletFilePath));
    }
    // 加载钱包文件
    public static void loadWallet(String walletFileName, String passWord) throws IOException, CipherException {
        String walleFilePath = walletFilePath + "/" + walletFileName;
        credentials = WalletUtils.loadCredentials(passWord, walleFilePath);
        String address = credentials.getAddress();
        BigInteger publicKey = credentials.getEcKeyPair().getPublicKey();
        BigInteger privateKey = credentials.getEcKeyPair().getPrivateKey();
        log.info("wallet info:");
        log.info("\taddress=" + address);
        log.info("\tpublic key=" + publicKey);
        log.info("\tprivate key=" + privateKey);
    }
    // 加载钱包文件
    // 加载合约
    public static void contractAddress(String contractAddress) throws Exception {
        ContractGasProvider contractGasProvider = new DefaultGasProvider();

        //合约初始化
        //需要指定chainId
        //修改 contract = PDCERC721IpfsManager.load(contractAddress, web3j, credentials, contractGasProvider);
        //为->
        TransactionManager tm = new RawTransactionManager(web3j, credentials, 8801);
        contract = new PDCERC721IpfsManager(contractAddress, 
                web3j, 
                tm, 
                BigInteger.valueOf(2000000000L), 
                BigInteger.valueOf(2100000));

        RemoteFunctionCall<String> contractName = contract.name();
        log.info("contract name: {}", contractName.send());
    }

    public static void mint_token() throws Exception {
        String cid = "QmWuamsTMwPnonuLh4MBxASymDB6YRiwq4Cic35rcxiFuQ";
        String to = "0xc9702898f44bD124712184DAeffbd2bf012e069B";
        log.info("signer address: {}", credentials.getAddress());
        RemoteFunctionCall<TransactionReceipt> result = contract.mint(to, cid);
        //发送交易
        TransactionReceipt tr = result.send();
        //获取mint时合约事件
        List<PDCERC721IpfsManager.RequestTransferEventResponse> rter = contract.getRequestTransferEvents(tr);
        log.info("block hash: " + tr.getTransactionHash());
        //显示事件内容
        log.info("size: " + rter.size());
        log.info("from: " + rter.get(0).from);
        log.info("to:   " + rter.get(0).to);
        log.info("tokenId: " + rter.get(0).tokenId); //tokenId
        log.info("cid: " + rter.get(0).datas);
    }

    // 获取账户tokenId()
    public static void showAccountTokens(String accountAddress) throws Exception {
        RemoteFunctionCall<BigInteger> response = contract.balanceOf(accountAddress);
        BigInteger result = response.send();
        log.info("balanceOf[{}]: {}", accountAddress, result);
        Integer c = Integer.valueOf(result.toString());
        for(Integer i = 0; i < c; i++) {
           getTokenByIndex(accountAddress, i);
        }
    }

    // 获取all token
    public static void showTokens() throws Exception {
        RemoteFunctionCall<BigInteger> response = contract.totalSupply();
        BigInteger result = response.send();
        log.info("totalSupply: {}", result);
        Integer c = Integer.valueOf(result.toString());
        for(Integer i = 0; i < c; i++) {
            RemoteFunctionCall<BigInteger> res_tid = contract.tokenByIndex(BigInteger.valueOf(i));
            BigInteger tokenId = res_tid.send();
            log.info("tokenId[{}:{}]: ", i, tokenId);

            RemoteFunctionCall<String> res_owner = contract.ownerOf(tokenId);
            String owner = res_owner.send();
            log.info("\towner: {}", owner);

            RemoteFunctionCall<BigInteger> res_timestamp = contract.timestampOfToken(tokenId);
            BigInteger timestamp = res_timestamp.send();
            log.info("\timestamp: {}", timestamp);

            RemoteFunctionCall<String> res_singer = contract.singerOfToken(tokenId);
            String singer = res_singer.send();
            log.info("\tsinger: {}", singer);

            RemoteFunctionCall<String> res_sender = contract.senderOfToken(tokenId);
            String sender = res_sender.send();
            log.info("\tsender: {}", sender);

            RemoteFunctionCall<String> res_datas = contract.datasOfToken(tokenId);
            String datas = res_datas.send();
            log.info("\tdatas: {}", datas);
        }
    }
    // 获取账户tokenId()
    public static BigInteger getTokenByIndex(String owner, Integer index) throws Exception {
        return getTokenByIndex(owner, BigInteger.valueOf(index));
    }

    public static BigInteger getTokenByIndex(String owner, BigInteger index) throws Exception {
        RemoteFunctionCall<BigInteger> response = contract.tokenOfOwnerByIndex(owner, index);
        BigInteger result = response.send();
        log.info("tokenId[{}:{}]: {}", owner, index, result);
        return result;
    }

    // 根据tokenid获取cid
    public static void getCidByTokenId() throws Exception {
        String tokenStr = "0";
        BigInteger tokenId = BigInteger.valueOf(Long.valueOf(tokenStr));
        RemoteFunctionCall<String> response = contract.getResponse(tokenId);
        String result = response.send();
        log.info("getCidByTokenId[{}]: {}", tokenStr, result);
    }

    // requests
    public static void getRequestTokenId() throws Exception {
        String tokenStr = "0";
        BigInteger tokenId = BigInteger.valueOf(Long.valueOf(tokenStr));
        RemoteFunctionCall<String> response = contract.getResponse(tokenId);
        String result = response.send();
        log.info("getCidByTokenId[{}]: {}", tokenStr, result);
    }

    // 转账
    public static void transto() throws Exception {
       String address_to = "0x89fF4a850e39A132614dbE517F80603b4A96fa0A"; 

       TransactionManager tm = new RawTransactionManager(web3j, credentials, 8801);//chainId=8801
       Transfer t = new Transfer(web3j, tm);
       TransactionReceipt send = t.sendFunds(address_to,
                BigDecimal.ONE, Convert.Unit.FINNEY, BigInteger.valueOf(20000000000L), BigInteger.valueOf(2100000)).send();

        log.info("Transaction complete:");
        log.info("trans hash=" + send.getTransactionHash());
        log.info("from :" + send.getFrom());
        log.info("to:" + send.getTo());
        log.info("gas used=" + send.getGasUsed());
        log.info("status: " + send.getStatus());
    }
}
