# sample-project-covid
This project is based on the sample project from line-spring-boot-kitchenshink
- GitHub link: https://github.com/line/line-bot-sdk-java


## Chatbot - COVID資訊+

###功能清單
- 目前Chatbot功能清單及來源資訊


Functions | Description | Resources | INPUT | OUTPUT | Fields
 --- | --- | --- |--- |--- |---
查本日 | 查詢台灣本日疫情狀況 | https://od.cdc.gov.tw/eic/covid19/covid19_tw_stats.csv | N/A | CSV | 0確診, 1死亡, 2送驗, 3排除, 4昨日確診, 5昨日排除, 6昨日送驗 |
查疫情-各縣市病例 | 查詢台灣各縣市病例詳情 | https://covid-19.nchc.org.tw/api.php?limited=台東縣&tableID=5001| 縣市名稱 (“台”) | JSON | {"id":"","a01":"個案研判日","a02":"縣市","a03":"鄉鎮","a04":"性別","a05":"是否為境外移入","a06":"年齡層"}  |
查疫情-各縣市疫苗施打 | 查詢台灣各縣市疫苗施打率 | https://covid-19.nchc.org.tw/api/covid19?CK=covid-19@nchc.org.tw&querydata=2001&limted=臺東縣| 縣市名稱 (“臺”) | JSON | {"id":"ID","a01":"日期","a02":"縣市別","a03":"(A) 總人口數","a04":"新增接種人次","a05":"(B) 累計接種人次","a06":"(B\/A) 疫苗覆蓋率 (%)","a07":"(C) 累計配送量 (劑)","a08":"(C-B) 剩餘量 (劑)","a09":"(C-B\/C) 剩餘量 (%)","a10":"AZ新增接種人次","a11":"AZ累計接種人次","a12":"AZ疫苗覆蓋率 (%)","a13":"AZ累計配送量 (劑)","a14":"AZ剩餘量 (劑)","a15":"AZ剩餘量 (%)","a16":"Moderna新增接種人次","a17":"Moderna累計接種人次","a18":"Moderna疫苗覆蓋率 (%)","a19":"Moderna累計配送量 (劑)","a20":"Moderna剩餘量 (劑)","a21":"Moderna剩餘量 (%)"} |
最新資訊 | 查詢疾管署最新資訊 | https://www.cdc.gov.tw/Bulletin/List/MmgtpeidAR5Ooai4-fgHzQ| N/A |  | |
