package com.example.bot.spring.covidcases;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.client.LineBlobClient;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.*;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static java.util.Collections.singletonList;

@Slf4j
@LineMessageHandler
public class CovidCasesController {

    @Autowired
    private LineMessagingClient lineMessagingClient;

    @Autowired
    private LineBlobClient lineBlobClient;


    @EventMapping
    public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws Exception {
        TextMessageContent message = event.getMessage();
        handleTextContent(event.getReplyToken(), message);
    }

    public enum City {

        TPE_NTPC_KEL("臺北、新北、基隆、宜蘭地區", new String[]{"臺北市", "新北市", "基隆市", "宜蘭縣"}, 0),
        TYN_HSZ_ZMI("桃園、新竹、苗栗地區",new String[]{"桃園市", "新竹縣", "新竹市", "苗栗縣"}, 1),
        TXG_CHW_NTC("臺中、彰化、南投地區",new String[]{"臺中市", "彰化縣", "南投縣"}, 2),
        YUN_CYI_TNN("雲林、嘉義、臺南地區",new String[]{"雲林縣","嘉義縣", "嘉義市", "臺南市"}, 3),
        KHH_PIF("高雄、屏東地區",new String[]{"高雄市","屏東縣"}, 4),
        HUN_TTT("花蓮、臺東地區",new String[]{"花蓮縣", "臺東縣"}, 5),
        PEH_KNH_LNN("金門、連江、澎湖地區",new String[]{"澎湖縣","金門縣", "連江縣"}, 6);

        City(String chinese, String[] cityNames, int code){
            this.chinese = chinese;
            this.cityNames = cityNames;
            this.code = code;
        }

        private final String chinese;
        private final String[] cityNames;
        private int code;

        public static City getCity(int i){
            for(City city : values()){
                if(city.getCode() == i) {
                    return city;
                }
            }
            return null;
        }

        public String getChinese(){
            return this.chinese;
        }

        public String[] getCityNames() {
            return this.cityNames;
        }

        public int getCode() {
            return this.code;
        }

    }
    @EventMapping
    public void handlePostbackEvent(PostbackEvent event) throws Exception {
        String replyToken = event.getReplyToken();
        String data = event.getPostbackContent().getData();

        String[] dataList = data.split("_");
        Integer cityCode = Integer.parseInt(dataList[1]);
        String[] citiesArray = City.getCity(cityCode).getCityNames();


        if (data.startsWith("Case_")){

            for (int index =0; index < citiesArray.length; index++){
                citiesArray[index] = citiesArray[index].replace("臺", "台");
            }
            String respond = this.getCasesByCities(new ArrayList<String>(Arrays.asList(citiesArray)));
            this.replyText(replyToken, respond);
        }
        else if (data.startsWith("Vaccination_")){
            String respond = this.getVaccRateByCities(new ArrayList<String>(Arrays.asList(citiesArray)));
            this.replyText(replyToken, respond);
        }
        else {
            this.replyText(replyToken,"Got postback data "+ event.getPostbackContent().getData());
        }
    }
    private void handleTextContent(String replyToken, TextMessageContent content)
            throws Exception {
        final String text = content.getText();
        log.info("Got text message from replyToken:{}: text:{} emojis:{}", replyToken, text,
                content.getEmojis());

        switch (text){
            case "最新消息": {
                URI imageUrl = createUri("/static/buttons/cdc_logo.jpg");
                ButtonsTemplate buttonsTemplate = new ButtonsTemplate(
                        imageUrl,
                        "最新消息",
                        "衛福部疾管署最新資訊",
                        Arrays.asList(
                                new URIAction("新聞稿",
                                        URI.create("https://www.cdc.gov.tw/Bulletin/List/MmgtpeidAR5Ooai4-fgHzQ"), null),
                                new URIAction("澄清專區",
                                        URI.create("https://www.cdc.gov.tw/Bulletin/List/MmgtpeidAR5Ooai4-fgHzQ"), null)
                        ));
                TemplateMessage templateMessage = new TemplateMessage("Button alt text", buttonsTemplate);
                this.reply(replyToken, templateMessage);
                break;
            }

            case "查疫情":
            case "查台灣疫情":
            case "查臺灣疫情": {
                ArrayList<CarouselColumn> carouselColumns = new ArrayList<>();
                for(int i=0; i<7; i++){
                    URI imageUrl = createUri("/static/buttons/map-"+ i +".png");
                    carouselColumns.add(new CarouselColumn(imageUrl, City.getCity(i).getChinese(), "查詢案例數,疫苗接種數", Arrays.asList(
                            new PostbackAction("累積案例數",
                                    "Case_" + i,
                                    City.getCity(i).getChinese() + "累積案例數"),
                            new PostbackAction("疫苗接種率",
                                    "Vaccination_" + i,
                                    City.getCity(i).getChinese() + "疫苗接種率")
                    )));
                }
                CarouselTemplate carouselTemplate = new CarouselTemplate(carouselColumns);
                TemplateMessage templateMessage = new TemplateMessage("Carousel alt text", carouselTemplate);
                this.reply(replyToken, templateMessage);
                break;
            }

//            case "查詢採檢醫院": {
//                String replyMsg = "";
//                // TODO
//                ArrayList<String> response = readHospitalCsv("https://od.cdc.gov.tw/icb/指定採檢醫院清單.csv", City.TPE_NTPC_KEL.getCityNames());
//                for (int i=0; i<response.size(); i++){
//                    replyMsg = replyMsg + "\n" + response.get(i) ;
//                }
//                this.replyText(replyToken,replyMsg);
//                break;
//            }

            case "查本日":{
                String link = "https://od.cdc.gov.tw/eic/covid19/covid19_tw_stats.csv";
                String response = readCsv(link);

                this.replyText(replyToken,response);
                break;
            }
            case "help":
            case "Help":
            case "HELP":{
                String response = "\uD83D\uDC47COVID資訊+ 指令教學\uD83D\uDC47\n\n" +
                        "1. 輸入【查本日】取得本日疫情最新，[確診數], [死亡數], [昨日新增] ... 等\n\n" +
                        "2. 輸入【查疫情】顯示各縣市疫情狀況，包括縣市 [累積總病例數] 及 [疫苗接種率]\n\n" +
                        "3. 輸入【最新消息】取得衛福部疾管署最新資訊\n\n" +
                        "4. 輸入【HELP】查詢所有指令";
                        this.replyText(replyToken,response);
                break;

            }
        }
    }

    private ArrayList<Case> jsonParse(String input) throws JsonProcessingException {
        String reply = input;
        ObjectMapper mapper = new ObjectMapper();
        ArrayList<Case> result = mapper.readValue(reply, new TypeReference<ArrayList<Case>>(){});
        return result;
    }
    private String readCsv(String link) throws Exception{

        URL url = new URL(link);
        HttpURLConnection http = (HttpURLConnection)url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()));

        String inputLine;
        String response = "⚠️【本日資訊】⚠️";

        //read the first line
        String firstLine = in.readLine();
        String titles[] = firstLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
        while((inputLine=in.readLine())!=null){
            String item[] = inputLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
            for (int i=0; i<item.length; i++){
                String data = item[i].trim().replace("\"","");
                response = response + "\n"+ titles[i] + ": " + data + " 例" ;
            }
        }

        in.close();
        http.disconnect();

        return response;
    }

    private ArrayList<String> readHospitalCsv(String link, String[] cities) throws Exception{

        URL url = new URL(link);
        HttpURLConnection http = (HttpURLConnection)url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream()));
        String line = reader.readLine();
        ArrayList<String> response = new ArrayList<>();
        while((line=reader.readLine())!=null){
            String item[] = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
            String cityName= item[2].trim();
            if (Arrays.asList(cities).contains(cityName)){

            }
            String name = item[4].trim();
            String address= item[5].trim();
            response.add(cityName + "-" + name + " 地址： " + address);
        }
        reader.close();
        http.disconnect();

        return response;
    }
    private String getCasesByCities(ArrayList<String> cities) throws Exception{
        ArrayList<Integer> cases = new ArrayList<>();
        for(int i=0; i < cities.size(); i++){
            // data: 5001 cases data
            String response = queryCityStatus(cities.get(i),"5001");
            ArrayList<Case> totalCase = jsonParse(response.toString());
            cases.add(totalCase.size());
        }
        String respond ="\uD83E\uDDA0【各縣市累積病例數】";
        for(int j=0; j < cases.size(); j++){
            respond = respond + "\n" + cities.get(j) + ": " + cases.get(j) + " 例";
        }
        return respond;
    }
    private String getVaccRateByCities(ArrayList<String> cities) throws Exception{
        ArrayList<String> cityRateList = new ArrayList<>();
        String date = "";
        for(int i=0; i < cities.size(); i++){
            // data: 2001 >> vaccination data
            String response = queryCityStatus(cities.get(i),"2001");
            // only need the newest data (the first element)
            JSONArray jsonArray = new JSONArray(response);

            if (jsonArray != null){
                JSONObject obj = jsonArray.getJSONObject(0);
                date = obj.get("a01").toString();
                String city = obj.get("a02").toString();
                String vacciRate = obj.get("a06").toString();
                cityRateList.add(city + ": " + vacciRate + " % ");
            }
        }
        String response ="\uD83D\uDC89【各縣市疫苗覆蓋率】\n - 截至" + date;
        for (int j=0; j < cityRateList.size(); j++){
            response = response +"\n" + cityRateList.get(j);
        }
        return response;
    }
    private String getResponseByHttpPost(String link, String data) throws Exception {
        URL url = new URL(link);
        getTrust();
        HttpURLConnection http = (HttpURLConnection)url.openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setRequestProperty("Content-Type", "application/json");

        byte[] out = data.getBytes(StandardCharsets.UTF_8);

        OutputStream stream = http.getOutputStream();
        stream.write(out);

        log.info("Post respond code: "+ http.getResponseCode() + " " + http.getResponseMessage());

        BufferedReader in = new BufferedReader(
                new InputStreamReader(http.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        http.disconnect();
        return response.toString();
    }

    private String queryCityStatus(String city, String queryData) throws Exception {
        String link ="https://covid-19.nchc.org.tw/api/covid19";
        String data = "{\"CK\":\"covid-19@nchc.org.tw\", \"querydata\":\""+queryData+"\", \"limited\":\"" + city + "\"}";
        String response;
        response = getResponseByHttpPost(link,data);
        return response;
    }

    private static void getTrust() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[] { new X509TrustManager() {

                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            } }, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reply(@NonNull String replyToken, @NonNull Message message) {
        reply(replyToken, singletonList(message));
    }

    private void reply(@NonNull String replyToken, @NonNull List<Message> messages) {
        reply(replyToken, messages, false);
    }

    private void reply(@NonNull String replyToken,
                       @NonNull List<Message> messages,
                       boolean notificationDisabled) {
        try {
            BotApiResponse apiResponse = lineMessagingClient
                    .replyMessage(new ReplyMessage(replyToken, messages, notificationDisabled))
                    .get();
            log.info("Sent messages: {}", apiResponse);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void replyText(@NonNull String replyToken, @NonNull String message) {
        if (replyToken.isEmpty()) {
            throw new IllegalArgumentException("replyToken must not be empty");
        }
        if (message.length() > 1000) {
            message = message.substring(0, 1000 - 2) + "……";
        }
        this.reply(replyToken, new TextMessage(message));
    }

    private static URI createUri(String path) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .scheme("https")
                .path(path).build()
                .toUri();
    }

    private static DownloadedContent createTempFile(String ext) {
        String fileName = LocalDateTime.now().toString() + '-' + UUID.randomUUID() + '.' + ext;
        Path tempFile = CovidCasesApplication.downloadedContentDir.resolve(fileName);
        tempFile.toFile().deleteOnExit();
        return new DownloadedContent(
                tempFile,
                createUri("/downloaded/" + tempFile.getFileName()));
    }

    @Value
    private static class DownloadedContent {
        Path path;
        URI uri;
    }
}
