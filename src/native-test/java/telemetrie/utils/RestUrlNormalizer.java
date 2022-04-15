package telemetrie.utils;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
public class RestUrlNormalizer {

  // special replacements of parameters in url
  private static final Pattern nameRegex = Pattern.compile("name=[^&]*", Pattern.CASE_INSENSITIVE);
  private static final Pattern prefixRegex =
      Pattern.compile("prefix=[^&]*", Pattern.CASE_INSENSITIVE);
  private static final Pattern sessionidRegex =
      Pattern.compile("(?:sessionid|bokeh-session-id)=[^&]*", Pattern.CASE_INSENSITIVE);
  private static final Pattern jwtRegex = Pattern.compile("([?/=])ey[A-Za-z0-9-_=]{12,}\\.[A-Za-z0-9-_=]{12,}\\.[A-Za-z0-9-_.+/=]*");
  private static final Pattern jwtHeaderRegex = Pattern.compile("([?/=])ey[A-Za-z0-9-_=]{12,}");
  private static final Map<String, Pattern> patterns = new HashMap<>();
  private static final Pattern az = Pattern.compile("[A-Za-z]+");
  /** Map of special replacements applied if the key matches */
  static List<SpecialReplacement> specialReplacements = new ArrayList<>();

  static {
    specialReplacements.add(
        SpecialReplacement.builder()
            .startsWith("/vvn/praemienrechner")
            .matchPattern(
                Pattern.compile(
                    "/vvn/praemienrechner/rest/praemien/eurotaxVehicles/([0-9]+)/([^/]+)",
                    Pattern.CASE_INSENSITIVE))
            .replacement("/praemien/eurotaxVehicles/$1/-generic-Term-")
            .sampleInput(
                "/vvn/praemienrechner/rest/praemien/eurotaxVehicles/10/AUDI S3 2.0 TFSI quattro 2014")
            .expectedResult("/praemien/eurotaxVehicles/{generic-Number}/-generic-Term-")
            .build());
    specialReplacements.add(
        SpecialReplacement.builder()
            .startsWith("/vvn/praemienrechner")
            .matchPattern(
                Pattern.compile(
                    "/vvn/praemienrechner/rest/praemien/eurotaxVehiclesCertificationNumber/([0-9]+)/([^(]+)",
                    Pattern.CASE_INSENSITIVE))
            .replacement("/praemien/eurotaxVehiclesCertificationNumber/$1/-generic-Term-")
            .sampleInput(
                "/vvn/praemienrechner/rest/praemien/eurotaxVehiclesCertificationNumber/10/1VD284")
            .expectedResult(
                "/praemien/eurotaxVehiclesCertificationNumber/{generic-Number}/-generic-Term-")
            .build());
    specialReplacements.add(
        SpecialReplacement.builder()
            .startsWith("/vvn/praemienrechner")
            .matchPattern(
                Pattern.compile(
                    "/vvn/praemienrechner/rest/praemien/plzOrt/([^/]+)", Pattern.CASE_INSENSITIVE))
            .replacement("/praemien/plzOrt/-generic-Term-")
            .sampleInput("/vvn/praemienrechner/rest/praemien/plzOrt/8180 Bülach")
            .expectedResult("/praemien/plzOrt/-generic-Term-")
            .build());
    specialReplacements.add(
        SpecialReplacement.builder()
            .startsWith("/vvn/praemienrechner")
            .matchPattern(
                Pattern.compile(
                    "/vvn/praemienrechner/rest/praemien/eurotaxTagsLimit/([^/]+)/([^/]+)",
                    Pattern.CASE_INSENSITIVE))
            .replacement("/praemienrechner/rest/praemien/eurotaxTagsLimit/$1/-generic-Term-")
            .sampleInput("/vvn/praemienrechner/rest/praemien/eurotaxTagsLimit/10/Bmw,320,d")
            .expectedResult(
                "/praemienrechner/rest/praemien/eurotaxTagsLimit/{generic-Number}/-generic-Term-")
            .build());
    specialReplacements.add(
        SpecialReplacement.builder()
            .startsWith("/vvn/praemienrechner")
            .matchPattern(
                Pattern.compile(
                    "/vvn/praemienrechner/rest/praemien/plzOrtTagsLimit/([^/]+)",
                    Pattern.CASE_INSENSITIVE))
            .replacement("/praemien/plzOrtTagsLimit/-generic-Term-")
            .sampleInput("/vvn/praemienrechner/rest/praemien/plzOrtTagsLimit/Malters")
            .expectedResult("/praemien/plzOrtTagsLimit/-generic-Term-")
            .build());
    specialReplacements.add(
        SpecialReplacement.builder()
            .startsWith("/map/gutschein")
            .matchPattern(
                Pattern.compile("/map/gutschein/rest/vouchers/([^/]+)", Pattern.CASE_INSENSITIVE))
            .replacement("/vouchers/-generic-Term-")
            .sampleInput("/map/gutschein/rest/vouchers/agrola-24")
            .expectedResult("/vouchers/-generic-Term-")
            .build());
    specialReplacements.add(
        SpecialReplacement.builder()
            .contains("/stacks/")
            .matchPattern(
                Pattern.compile(
                    "(.*)/stacks/([^/]+)", Pattern.CASE_INSENSITIVE))
            .replacement("$1/stacks/-generic-Term-")
            .sampleInput("/kis/protokollierung/rest/stacks/BE07_070619_002")
            .expectedResult("/kis/protokollierung/rest/stacks/-generic-Term-")
            .build());

    specialReplacements.add(
        SpecialReplacement.builder()
            .contains("/stacks/")
            .contains("/processes/")
            .matchPattern(
                Pattern.compile(
                    "(.*)/stacks/([^/]+)/processes/([^/]+)", Pattern.CASE_INSENSITIVE))
            .replacement("$1/stacks/-generic-Term-/processes/-generic-Term-")
            .sampleInput("/kis/protokollierung/rest/stacks/BE07_211220_012/processes/BE07_211220_012_0004")
            .expectedResult("/stacks/-generic-Term-/processes/-generic-Term-")
            .build());

    specialReplacements.add(
        SpecialReplacement.builder()
            .startsWith("/kis/protokollierung")
            .matchPattern(
                Pattern.compile(
                    "/kis/protokollierung/rest/stacks/([^/]+)/processes", Pattern.CASE_INSENSITIVE))
            .replacement("/stacks/-generic-Term-/processes")
            .sampleInput("/kis/protokollierung/rest/stacks/BE07_070619_002/processes")
            .expectedResult("/stacks/-generic-Term-/processes")
            .build());
    specialReplacements.add(
        SpecialReplacement.builder()
            .contains("/treatments/")
            .matchPattern(
                Pattern.compile(
                    "(.*)/treatments/([^/]+)",
                    Pattern.CASE_INSENSITIVE))
            .replacement("$1/treatments/-generic-Term-")
            .sampleInput(
                "/jap/featuretoggle-evaluator/rest/treatments/flc-999-enables-new-ovn-objekt-service-for-mcs (GET)")
            .expectedResult("/jap/featuretoggle-evaluator/rest/treatments/-generic-Term-")
            .build());
    specialReplacements.add(
        SpecialReplacement.builder()
            .startsWith("/dtm/logisticsmgmt-service/api/replications")
            .matchPattern(
                Pattern.compile(
                    "/dtm/logisticsmgmt-service/api/replications\\?from=([^&]+)(.*)",
                    Pattern.CASE_INSENSITIVE))
            .replacement("/dtm/logisticsmgmt-service/api/replications?from=-date-$2")
            .sampleInput(
                "/dtm/logisticsmgmt-service/api/replications?from=2019-08-14T12:46:20.791Z (GET)")
            .expectedResult("/dtm/logisticsmgmt-service/api/replications?from=-date-")
            .build());

    // special replacement for sip uris (sip: and sips:)
    SpecialReplacement sipReplacement =
        SpecialReplacement.builder()
            .contains("sip:")
            .matchPattern(
                Pattern.compile(
                    "(?<before>.*?)"
                        + "(?<scheme>\\w+):" // Scheme
                        + "(?:(?<user>[\\w\\.]+):?(?<password>[\\w\\.]+)?@)?" // User:Password
                        + "\\[?(?<host>" //Begin group host
                        + "(?:\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})|" //IPv4 address Host Or
                        + "(?:(?:[0-9a-fA-F]{1,4}):){7}[0-9a-fA-F]{1,4}|" //IPv6 address Host Or
                        + "(?:(?:[0-9A-Za-z]+\\.)+[0-9A-Za-z]+)" //"#Hostname string
                        + ")\\]?:?" //End group host
                        + "(?<port>\\d{1,6})?" // port
                        + "(?:\\;(?<params>[^\\?]*))?" // parameters
                        + "(?:\\?(?<headers>.*))?"//headers
                        + "(?<after>.*?)",
                    Pattern.CASE_INSENSITIVE))
            .replacement("${before}sip-uri${after}")
            .sampleInput(
                "/cti-lookup/sip:p.a.mueller@bluewin.ch")
            .expectedResult("/cti-lookup/sip-uri")
            .build();
    specialReplacements.add(sipReplacement);
    specialReplacements.add(sipReplacement.toBuilder().contains("sips:").build());

    specialReplacements.add(
        SpecialReplacement.builder()
            .contains("/trip/")
            .matchPattern(
                Pattern.compile("(.*)/trip/([0-9A-Z]{28,})(/?.*)", Pattern.CASE_INSENSITIVE))
            .replacement("$1/trip/-trip-Number-/$3")
            .sampleInput(
                "/iot/fahrtenschreiber-trips/rest/trips/contract/15814916/trip/LZ3BG5RCJHJWJ511604949985707/waypoints (GET)")
            .expectedResult(
                "/iot/fahrtenschreiber-trips/rest/trips/contract/{generic-Number}/trip/-trip-Number-/waypoints (GET)")
            .build());
    specialReplacements.add(
        SpecialReplacement.builder()
            .contains("/trips/")
            .matchPattern(
                Pattern.compile("(.*)/trips/([0-9A-Za-z=]{28,})(/?.*)", Pattern.CASE_INSENSITIVE))
            .replacement("$1/trips/-trip-Number-/$3")
            .sampleInput(
                "/iota/mftelematik/rest/contracts/G-1480-1696/trips/VzNYNFI3VU1IOVowMEVQMTYwNTAwNTUzOTI0MjsyMDIwLTExLTEwVDExOjM3OjE2OzIwMjAtMTEtMTBUMTE6Mzc6MjY7MTQ4MDE2OTY=/detail (GET)")
            .expectedResult(
                "/iota/mftelematik/rest/contracts/{generic-G-Id}/trips/-trip-Number-/detail (GET)")
            .build());


    specialReplacements.add(
        SpecialReplacement.builder()
            .contains("/kontaktadress-praeferenzen/")
            .matchPattern(
                Pattern.compile("/kontaktadress-praeferenzen/(KM[0-9]{12})(.*)", Pattern.CASE_INSENSITIVE))
            .replacement("/kontaktadress-praeferenzen/-praeferenz-Number-$2")
            .sampleInput(
                "/kontaktadress-praeferenzen/KM131311480003 (GET)")
            .expectedResult(
                "/kontaktadress-praeferenzen/-praeferenz-Number- (GET)")
            .build());
    specialReplacements.add(
        SpecialReplacement.builder()
            .contains("/kontaktadress-praeferenzen-elektronisch/")
            .matchPattern(
                Pattern.compile(
                    "/kontaktadress-praeferenzen-elektronisch/(KM[0-9]{12})(.*)",
                    Pattern.CASE_INSENSITIVE))
            .replacement("/kontaktadress-praeferenzen-elektronisch/-praeferenz-Number-$2")
            .sampleInput(
                "/pdv/partnerverwaltung/rest/mikroedit/oms/kontaktadress-praeferenzen-elektronisch/KM151364880004 (PUT)")
            .expectedResult(
                "/pdv/partnerverwaltung/rest/mikroedit/oms/kontaktadress-praeferenzen-elektronisch/-praeferenz-Number- (PUT)")
            .build());

    specialReplacements.add(
        SpecialReplacement.builder()
            .contains("/account")
            .matchPattern(
                Pattern.compile("(.*)/[0-9]{6}[A-Z]((?:/| ).*)", Pattern.CASE_INSENSITIVE))
            .replacement("$1/-account-Number-$2")
            .sampleInput("/api/internal/portfolio-segments/123456/accounts/102208A (GET)")
            .expectedResult(
                "/api/internal/portfolio-segments/{generic-Number}/accounts/-account-Number- (GET)")
            .build());

    specialReplacements.add(
        SpecialReplacement.builder()
            .contains("/partner-verifications")
            .matchPattern(
                Pattern.compile("(.*)/(?:(?:[0-9]{3}-){3}[0-9]{3})(.*)", Pattern.CASE_INSENSITIVE))
            .replacement("$1/-verification-Number-$2")
            .sampleInput("/partner-verifications/113-919-838-278 (PATCH)")
            .expectedResult(
                "/partner-verifications/-verification-Number- (PATCH)")
            .build());


    specialReplacements.add(
        SpecialReplacement.builder()
            .contains("/documents")
            .matchPattern(
                Pattern.compile("(.*)/documents/(?:(?:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})|(?:[0-9a-z-:_]+))(.*)", Pattern.CASE_INSENSITIVE))
            .replacement("$1/documents/-document-Number-$2")
            .sampleInput("/mcm/daten/rest/documents/090013a68252b8ad/profiles/MyMobiliarLifeObjectId/formats/original/disposition/inline (GET)")
            .expectedResult(
                "/mcm/daten/rest/documents/-document-Number-/profiles/MyMobiliarLifeObjectId/formats/original/disposition/inline (GET)")
            .sampleInput("T/preprod T/PROVIDER/REMOTE/map@core@map-core-b2c-app/mym@document@mym-document-service@/documents/-document-Number--doc:de_7978f52b-54c3-44ce-89ef-84dc467e412c (GET)")
            .expectedResult("T/preprod T/PROVIDER/REMOTE/map@core@map-core-b2c-app/mym@document@mym-document-service@/documents/-document-Number- (GET)")
            .build());

    specialReplacements.add(
        SpecialReplacement.builder()
            .contains("/copies")
            .matchPattern(
                Pattern.compile("(.*)/copies/(?:(?:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})|(?:[0-9a-z]+))(.*)", Pattern.CASE_INSENSITIVE))
            .replacement("$1/copies/-copy-Number-$2")
            .sampleInput("/copies/090013ec8da2af11/profiles/mks_schaden (POST)")
            .expectedResult(
                "/copies/-copy-Number-/profiles/mks_schaden (POST)")
            .build());

    specialReplacements.add(
        SpecialReplacement.builder()
            .contains("/campaigns")
            .matchPattern(
                Pattern.compile("(.*)/campaigns/(?:[0-9a-z]+)(.*)", Pattern.CASE_INSENSITIVE))
            .replacement("$1/campaigns/-campaign-Number-$2")
            .sampleInput("/campaigns/a2be927ae7a82d2fdba7fef66e3d6878ee0333ba30a82c31160c90b8d9050093/participants (GET)")
            .expectedResult(
                "/campaigns/-campaign-Number-/participants (GET)")
            .build());

    specialReplacements.add(
        SpecialReplacement.builder()
            .contains("/campaigns")
            .matchPattern(
                Pattern.compile("(.*)/campaigns/(?:[0-9a-z]+)(.*)", Pattern.CASE_INSENSITIVE))
            .replacement("$1/campaigns/-campaign-Number-$2")
            .sampleInput("/campaigns/a2be927ae7a82d2fdba7fef66e3d6878ee0333ba30a82c31160c90b8d9050093/participants (GET)")
            .expectedResult(
                "/campaigns/-campaign-Number-/participants (GET)")
            .build());

    specialReplacements.add(
        SpecialReplacement.builder()
            .contains("/socket.io")
            .matchPattern(
                Pattern.compile("(.*)/socket\\.io(?:/?\\?.*?) (.*)", Pattern.CASE_INSENSITIVE))
            .replacement("$1/socket.io $2")
            .sampleInput("/cog/ui/api/eventsource/cases/progress/socket.io?EIO=3u0026transport=pollingu0026t=NMji5Flu0026sid=kH_FiWRPROWeyUOsAAGP (GET)")
            .expectedResult(
                "/cog/ui/api/eventsource/cases/progress/socket.io (GET)")
            .build());

    specialReplacements.add(
        SpecialReplacement.builder()
            .contains("/schaden/hergang/OnlineSchadenmeldung")
            .matchPattern(
                Pattern.compile("/schaden/hergang/OnlineSchadenmeldung:(?:.*) (.*)", Pattern.CASE_INSENSITIVE))
            .replacement("/schaden/hergang/OnlineSchadenmeldung:-meldung- $1")
            .sampleInput(
                "/schaden/hergang/OnlineSchadenmeldung: Laterne gerammt, Vertrag 04277142 ok. Id=e0ac0e4c-02ec-4409-b01e-66be97d10646% (GET)")
            .expectedResult("/schaden/hergang/OnlineSchadenmeldung:-meldung- (GET)")
            .build());

    specialReplacements.add(
        SpecialReplacement.builder()
            .contains("/pdfs/")
            .matchPattern(
                Pattern.compile(
                    "(.*)/pdfs/(?:[0-9a-zA-Z-]*\\.pdf)", Pattern.CASE_INSENSITIVE))
            .replacement("$1/pdfs/-pdf-.pdf")
            .sampleInput(
                "/mcs/complexclaimsurveymatcher/static/pdfs/150119386929-0609f841-5921-411f-a1da-99016f981d01.pdf (GET)")
            .expectedResult("/mcs/complexclaimsurveymatcher/static/pdfs/-pdf-.pdf (GET)")
            .build());

    specialReplacements.add(
        SpecialReplacement.builder()
            .contains("?stackId=")
            .matchPattern(
                Pattern.compile("(.*)/?stackId=(?:[0-9a-zA-Z-_]*)(.*)", Pattern.CASE_INSENSITIVE))
            .replacement("$1/?stackid=-stack-id-$2")
            .sampleInput("/search?stackId=PR02_BE01_20201202_085056_0000 (GET)")
            .expectedResult("/search?/?stackid=-stack-id- (GET)")
            .build());

    specialReplacements.add(
        SpecialReplacement.builder()
            .contains("predict?keywords=")
            .matchPattern(
                Pattern.compile(
                    "(.*)/predict\\?keywords=(?:[0-9a-zA-Z-_]*?)u0026(.*)(threshold=[0-9]+(?:\\.[0-9]+)?-[0-9]+(?:\\.[0-9]+)?)",
                    Pattern.CASE_INSENSITIVE))
            .replacement("$1/predict?keywords=-predict-params-&$2threshold=-ragnge-")
            .sampleInput(
                "/predict?keywords=Baumu0026schadenbranche=19u0026schadenursache=72u0026threshold=0.95-1 (GET)")
            .expectedResult("/predict?keywords=-predict-params-&schadenbranche={generic-Number}&schadenursache={generic-Number}&threshold=-ragnge- (GET)")
            .build());

    specialReplacements.add(
        SpecialReplacement.builder()
            .contains("fileChanged?url=")
            .matchPattern(
                Pattern.compile(
                    "(.*)/fileChanged\\?url=(?:[0-9a-zA-Z-_.]*?)u0026(.*)",
                    Pattern.CASE_INSENSITIVE))
            .replacement("$1/fileChanged?url=-file-&$2")
            .sampleInput(
                "/sca/sca-csvfileimport-producer-service/fileChanged?url=2020-12-01_certificates.csvu0026batchKennung=STWu0026runId=093025501 (GET)")
            .expectedResult("/sca/sca-csvfileimport-producer-service/fileChanged?url=-file-&batchKennung=STW&runId={generic-Number} (GET)")
            .build());

    specialReplacements.add(
        SpecialReplacement.builder()
            .contains("/iban/")
            .matchPattern(
                Pattern.compile(
                    "(.*)/iban/(?:[a-zA-Z]{2}(?:0[2-9]|[1-8][0-9]|9[0-8])[^ ]+)",
                    Pattern.CASE_INSENSITIVE))
            .replacement("$1/iban/---IBAN+++")
            .sampleInput("/sss/banken/rest/iban/CH14090000003 (GET)")
            .expectedResult("/sss/banken/rest/iban/{IBAN} (GET)")
            .build());

    // ok
    specialReplacements.add(
            SpecialReplacement.builder()
                    .contains("/visumerteilungen/")
                    .matchPattern(
                            Pattern.compile(
                                    "(.*)/visumerteilungen/[0-9A-z-_.]+",
                                    Pattern.CASE_INSENSITIVE))
                    .replacement("$1/visumerteilungen/---generic-Visum-Name+++")
                    .sampleInput("/visumerteilungen/VISUM_FUER_DIREKTIONSRABATT_660011233917_c1414336-3c5c-4053-95fc-183b86807177_7.89_rabatt (PUT)")
                    .expectedResult("/visumerteilungen/{generic-Visum-Name} (PUT)")
                    .build());

    //ok
    specialReplacements.add(
            SpecialReplacement.builder()
                    .contains("/repositories/")
                    .matchPattern(
                            Pattern.compile(
                                    "(.*)(\\?)query=[0-9A-z-_.+%*{}]+",
                                    Pattern.CASE_INSENSITIVE))
                    .replacement("$1$2query=---generic-Query+++")
                    .sampleInput("/dtm-enterprisedatacatalog-graphdb-service/repositories/00524b74-47cc-42d4-a875-ac829dbf4e2a?query=PREFIX+rdf%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F1999%2F02%2F22-rdf-syntax-ns%23%3E%0APREFIX+rsSchema%3A+%3Chttps%3A%2F%2Fschema.mobicorp.ch%2Fedc%2Fresourcelinker%2F%3E%0Aselect+%3FsupplierEntity+%3FreferenceEntity+%3Fcluster+%28%22IPM%22+as+%3FlinkLabel%29+%3FmatchLevel+%3FtokenBaseProperty+%3Ftoken+where+%7B%0A}}++%3FsupplierEntity+rdf%3Atype+%3Chttps%3A%2F%2Fschema.mobicorp.ch%2Fadok%2FApplikation%3E+.%0A}}++%3FsupplierEntity+rsSchema%3AclusterReference+%3FclusterReference+.%0A}}++%3FclusterReference+rsSchema%3AbaseProperty+%22https%3A%2F%2Fschema.mobicorp.ch%2Fadok%2FIPM%22+.%0A}}++%3FclusterReference+rsSchema%3Acluster+%3Fcluster+.%0A}}++%3FclusterReference+rsSchema%3AlinkLabel+%22IPM%22+.%0A}}++%3Fcluster+rsSchema%3AtokenRef+%3Ftoken+.%0A}}}}SERVICE+%3Crepository%3Aedc-graphdb%3E+%7B%0A}}}}}+%3FreferenceEntity+rsSchema%3AtokenDecoration+%3FtokenDeco+.%0A}}}}}+%3FreferenceEntity+rdf%3Atype+%3Chttps%3A%2F%2Fschema.mobicorp.ch%2FPerson%3E+.%0A}}}}}+%3FtokenDeco+rsSchema%3AbaseProperty+%3FtokenBaseProperty+.%0A}}}}}+%3FtokenDeco+rsSchema%3AmatchLevel+%3FmatchLevel+.%0A}}}}}+%3FtokenDeco+rsSchema%3Atoken+%3Ftoken+.%0A}+%7D%7D%0AORDER+BY+ASC+%28%3FsupplierEntity%29+ASC+%28%3Fcluster%29+ASC+%28%22IPM%22%29+ASC+%28%3FmatchLevel%29++ASC+%28%3FreferenceEntity%29 (GET)")
                    .expectedResult("/dtm-enterprisedatacatalog-graphdb-service/repositories/{generic-UUID}?query={generic-Query} (GET)")
                    .build());

    specialReplacements.add(
            SpecialReplacement.builder()
                    .contains("/gestionnaires-prestation-responsable")
                    .matchPattern(
                            Pattern.compile(
                                    "(.*)nomLese=[0-9A-z-_. ]+ (.*)",
                                    Pattern.CASE_INSENSITIVE))
                    .replacement("$1nomLese=---generic-Name+++ $2")
                    .sampleInput("/api/v1/gestionnaires-prestation-responsable?numeroDossierSylber=4574&nomLese=Meier Stutz (GET)")
                    .expectedResult("/api/v1/gestionnaires-prestation-responsable?numeroDossierSylber={generic-Number}&nomLese={generic-Name} (GET)")
                    .build());

    specialReplacements.add(
            SpecialReplacement.builder()
                    .contains("/bbibbviewer/")
                    .matchPattern(
                            Pattern.compile(
                                    "(.*)filter=[0-9A-z-_.%]+",
                                    Pattern.CASE_INSENSITIVE))
                    .replacement("$1filter=---generic-Filter+++")
                    .sampleInput("/uwn/bbibbviewer/requirements?document_type_id=7987&version_ids=7978&page=78&page_size=8&filter=%C3%BCberschussbeteiligung (GET)")
                    .expectedResult("/uwn/bbibbviewer/requirements?document_type_id={generic-Number}&version_ids={generic-Number}&page={generic-Number}&page_size={generic-Number}&filter={generic-Filter} (GET)")
                    .build());

    //ok
    specialReplacements.add(
            SpecialReplacement.builder()
                    .contains("/zahlweisebeziehungen/")
                    .matchPattern(
                            Pattern.compile(
                                    "(.*)\\/zahlweisebeziehungen\\/[0-9A-z-_]+",
                                    Pattern.CASE_INSENSITIVE))
                    .replacement("$1/zahlweisebeziehungen/---generic-Name+++")
                    .sampleInput("/produktdefinitionen/artefakte/zahlweisebeziehungen/DIRECT_SALES_MOBICASA_NT (GET)")
                    .expectedResult("/produktdefinitionen/artefakte/zahlweisebeziehungen/{generic-Name} (GET)")
                    .build());

    // ok
    specialReplacements.add(
            SpecialReplacement.builder()
                    .contains("/artefakte/MOBITOUR/")
                    .matchPattern(
                            Pattern.compile(
                                    "(.*)\\/artefakte\\/MOBITOUR\\/[0-9A-z-_]+",
                                    Pattern.CASE_INSENSITIVE))
                    .replacement("$1/artefakte/MOBITOUR/---generic-Name+++")
                    .sampleInput("/artefakte/MOBITOUR/TARIF_BAUSTEIN (GET)")
                    .expectedResult("/artefakte/MOBITOUR/{generic-Name} (GET)")
                    .build());

    //ok
    specialReplacements.add(
            SpecialReplacement.builder()
                    .contains("/visumdeaktivierungen/")
                    .matchPattern(
                            Pattern.compile(
                                    "(.*)/visumdeaktivierungen/[0-9A-z-_.]+",
                                    Pattern.CASE_INSENSITIVE))
                    .replacement("$1/visumdeaktivierungen/---generic-Visum-Name+++")
                    .sampleInput("/faelle/6879/visumdeaktivierungen/VISUM_FUER_DIREKTIONSRABATT_660020545881-c13f-4045-bdbb-2ee0d5996a80_5_zuschlag.52_rabatt (PUT)")
                    .expectedResult("/faelle/{generic-Number}/visumdeaktivierungen/{generic-Visum-Name} (PUT)")
                    .build());

    // NO
    specialReplacements.add(
            SpecialReplacement.builder()
                    .contains("/cog/ui/")
                    .matchPattern(
                            Pattern.compile("(.*)/cog/ui/[0-9A-z-_.]+\\.js", Pattern.CASE_INSENSITIVE))
                    .replacement("$1/cog/ui/---generic-JS-File+++")
                    .sampleInput("/cog/ui/polyfills-es2015.d19c2c154066961ea47b.js (GET)")
                    .expectedResult(
                            "/cog/ui/{generic-JS-File} (GET)")
                    .build());

    // NO
    specialReplacements.add(
            SpecialReplacement.builder()
                    .contains("/cog/ui/")
                    .matchPattern(
                            Pattern.compile("(.*)/cog/ui/[0-9A-z-_.]+\\.css", Pattern.CASE_INSENSITIVE))
                    .replacement("$1/cog/ui/---generic-CSS-File+++")
                    .sampleInput("/cog/ui/polyfills-es2015.d19c2c154066961ea47b.css (GET)")
                    .expectedResult(
                            "/cog/ui/{generic-CSS-File} (GET)")
                    .build());

    // NO
    specialReplacements.add(
            SpecialReplacement.builder()
                    .contains("/cog/ui/")
                    .matchPattern(
                            Pattern.compile("(.*)/cog/ui/[0-9A-z-_.]+\\.svg", Pattern.CASE_INSENSITIVE))
                    .replacement("$1/cog/ui/---generic-SVG-File+++")
                    .sampleInput("/cog/ui/polyfills-es2015.d19c2c154066961ea47b.svg (GET)")
                    .expectedResult(
                            "/cog/ui/{generic-SVG-File} (GET)")
                    .build());

    specialReplacements.add(
            SpecialReplacement.builder()
                    .contains("/dependencies/")
                    .matchPattern(
                            Pattern.compile("(.*)/dependencies/([A-z]+)(?:\\/?\\?.*?) (.*)", Pattern.CASE_INSENSITIVE))
                    .replacement("$1/dependencies/$2 $3")
                    .sampleInput("/dependencies/matrix?classes=mobi:edc%2FSlurpRun&classes=mobi:edc%2FSlurpNamedGraphUpdate&classes=schema:Role&classes=adok:relation%2FRESTCall&classes=schema:Organization&classes=adok:relation%2FUsesRelationship&classes=schema:Person&classes=adok:tk%2FContainerService&classes=mobi:bvz%2FDatensammlung&classes=adok:tk%2FSWStream&graphURI=&mode=all (GET)")
                    .expectedResult(
                            "/dependencies/matrix (GET)")
                    .build());

    specialReplacements.add(
            SpecialReplacement.builder()
                    .contains("/explore-graph/")
                    .matchPattern(
                            Pattern.compile("(.*)/explore-graph/([A-z]+)(?:\\/?\\?.*?) (.*)", Pattern.CASE_INSENSITIVE))
                    .replacement("$1/explore-graph/$2 $3")
                    .sampleInput("/explore-graph/node?config=default&includeInferred=true&iri=https:%2F%2Fschema.mobicorp.ch%2Fadok%2FAPM&sameAsState=true (GET)")
                    .expectedResult(
                            "/explore-graph/node (GET)")
                    .build());

    // NO
    specialReplacements.add(
            SpecialReplacement.builder()
                    .contains("/search/cases")
                    .matchPattern(
                            Pattern.compile("(.*)search_term=[A-z0-9=]+(.*)", Pattern.CASE_INSENSITIVE))
                    .replacement("$1search_term=---generic-Search-Term+++$2")
                    .sampleInput("/cog/ui/api/search/cases?search_term=S8O8bg==&size=4362&page=4563456&caseid=46426&includenulldatesdocuments=true&sortby=docId&sortorder=ASC (GET)")
                    .expectedResult(
                            "/cog/ui/api/search/cases?search_term={generic-Search-Term}&size={generic-Number}&page={generic-Number}&caseid={generic-Number}&includenulldatesdocuments=true&sortby=docId&sortorder=ASC (GET)")
                    .build());

    specialReplacements.add(
            SpecialReplacement.builder()
                    .contains("wartungsseiten/pages/vp")
                    .matchPattern(
                            Pattern.compile("(.*)/pages/vp(?:\\/?\\?.*?) (.*)", Pattern.CASE_INSENSITIVE))
                    .replacement("$1/pages/vp $2")
                    .sampleInput(
                            "/ioa/wartungsseiten/pages/vp?type=js&session=v_4_srv_8_sn_66DEF3C1E61E4946DBCF2183AC83BAA2_perc_100000_ol_0_mul_1_app-3A642fc57b41d522ee_1&svrid={generic-Number}&flavor=post&visitID=FGMNIKEQGGHUMPCCDQCOPBOJUCFGWOFI-0&modifiedSince={generic-Number}&referer=https%3A%2F%2Fvp-b.mobicorp.test%2Fsiebel%2Fapp%2Ffins%2Fdeu%3FSWECmd%3DGotoView&SWEView%3DVisible%2BContact%2BList%2BView=null&SWERF%3D1=null&SWEHo%3D=null&SWEBU%3D1=null&app=642fc57b41d522ee&crc={generic-Number}&end={generic-Number} (GET)")
                    .expectedResult("/ioa/wartungsseiten/pages/vp (GET)")
                    .build());

  }


  static { // comment #matches of representative sample
    // 11339498
    patterns.put("{generic-Number}", Pattern.compile("^(\\-|\\+)?[0-9]+(?:\\.[0-9]+)?$"));
    // 4940279
    patterns.put("{generic-UNumber}", Pattern.compile("^U[0-9]{6}$", Pattern.CASE_INSENSITIVE));
    // 2460011
    patterns.put(
        "{generic-UUID}",
        Pattern.compile("^\\{?[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\}?$"));
    // 965204
    patterns.put("{generic-PNumber}", Pattern.compile("^P[0-9]{5,}$", Pattern.CASE_INSENSITIVE));
    // 105983
    patterns.put("{generic-Date}", Pattern.compile("^[0-9]{4}-[0-9]{2}-[0-9]{2}$"));
    // 239200
    patterns.put("{generic-ZNumber}", Pattern.compile("^Z[0-9]{6}$", Pattern.CASE_INSENSITIVE));
    // 94052
    patterns.put("{generic-GNumber}", Pattern.compile("^G[0-9]{7,8}$", Pattern.CASE_INSENSITIVE));
    // 92069
    patterns.put("{generic-3PartNumber}", Pattern.compile("^[0-9]+\\.[0-9]+\\.[0-9]+$"));
    // 36118
    patterns.put(
        "{generic-DateTime}",
        Pattern.compile(
            "^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}.*[0-9]{2}.*[0-9]{2}(?:\\.[0-9]*)?$",
            Pattern.CASE_INSENSITIVE));
    // 35914
    patterns.put("{generic-ONumber}", Pattern.compile("^O[0-9]{10}$", Pattern.CASE_INSENSITIVE));
    // 35914
    patterns.put("{generic-QONumber}", Pattern.compile("^QO[0-9]{8}$", Pattern.CASE_INSENSITIVE));
    // 15935
    patterns.put("{generic-32hash}", Pattern.compile("^[A-Za-z0-9]{32}$"));
    // 8986
    patterns.put("{generic-4PartNumber}", Pattern.compile("^[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+$"));
    // 6811
    patterns.put("{generic-96hash}", Pattern.compile("^[A-Za-z0-9]{96}$"));
    patterns.put("{generic-128hash}", Pattern.compile("^[A-Za-z0-9]{128}$"));
    // 2492
    patterns.put("{generic-ZMNumber}", Pattern.compile("^ZM[0-9]{8}$", Pattern.CASE_INSENSITIVE));
    // 1440
    patterns.put("{generic-CNumber}", Pattern.compile("^C[0-9]{7}$", Pattern.CASE_INSENSITIVE));
    // 1440
    patterns.put("{generic-KNumber}", Pattern.compile("^K[0-9]{4,}$", Pattern.CASE_INSENSITIVE));
    patterns.put("{generic-KMNumber}", Pattern.compile("^K[0-9]{8,}$", Pattern.CASE_INSENSITIVE));
    patterns.put("{generic-CHNumber}", Pattern.compile("^CH[0-9]{10}$", Pattern.CASE_INSENSITIVE));
    patterns.put("{generic-PPNumber}", Pattern.compile("^PP[0-9]{6,}$", Pattern.CASE_INSENSITIVE));
    patterns.put(
        "{generic-KPMNumber}", Pattern.compile("^KPM[0-9]{12}$", Pattern.CASE_INSENSITIVE));
    patterns.put(
        "{generic-G-Id}", Pattern.compile("^G-[0-9]{4}-[0-9]{4}$", Pattern.CASE_INSENSITIVE));
    patterns.put("{generic-GGNumber}", Pattern.compile("^GG[0-9]{8}$", Pattern.CASE_INSENSITIVE));
    patterns.put(
        "{generic-GEAR-UUID}",
        Pattern.compile(
            "^GEAR-\\{?[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\}?$"));
    patterns.put(
        "{generic-UUID-ID}",
        Pattern.compile(
            "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}-[0-9]+$"));

    patterns.put("{enveloppe}", Pattern.compile("^[0-9]{6}[A-Z]$", Pattern.CASE_INSENSITIVE));
    patterns.put("{NY-Number}", Pattern.compile("^NY[0-9]{2}_[0-9]{6}_[0-9]{3}_[0-9]{4}$", Pattern.CASE_INSENSITIVE));
    patterns.put(
        "{IBAN}",
        Pattern.compile(
            "^[a-zA-Z]{2}(?:0[2-9]|[1-8][0-9]|9[0-8])[a-zA-Z0-9]{4}[0-9]{6}[a-zA-Z0-9]{0,20}$",
            Pattern.CASE_INSENSITIVE));

    patterns.put("{generic-SP-Number}", Pattern.compile("SP_[0-9]+"));
  }

  public static String normalize(String restUrlMeasurePoint) {
    // very component specific replacements
    for (final SpecialReplacement sr : specialReplacements) {
      if (!empty(sr.startsWith) && restUrlMeasurePoint.startsWith(sr.startsWith)
          || !empty(sr.contains) && restUrlMeasurePoint.contains(sr.contains)) {
        restUrlMeasurePoint =
            sr.matchPattern.matcher(restUrlMeasurePoint).replaceAll(sr.replacement);
      }
    }
    return normalizeBase(restUrlMeasurePoint)
        .replace("---", "{")
        .replace("+++", "}");
  }

  /**
   * handy empty function for string.
   * @param s string to check
   * @return true if empty
   */
  public static boolean empty( final String s ) {
    // Null-safe, short-circuit evaluation.
    return s == null || s.trim().isEmpty();
  }
  /**
   * Special case for measure points starting with spa-nav, we apply the same logic that goes into
   * rest urls for parts of the measure point which contains some 2 rest urls
   */
  public static String normalizeSpaNav(final String measurePoint) {
    if (!measurePoint.startsWith("spa-nav")) {
      return measurePoint;
    }

    final StringJoiner normalizedMeasurePoint = new StringJoiner(" ");

    for (final String s : measurePoint.split(" ")) {
      final String[] list = s.split(":");
      for (int i = 0; i < list.length; i++) {
        // if there's a ':' we apply the logic to the right part
        if (i == 1) {
          list[i] = normalizeBase(list[i]);
        }
      }
      normalizedMeasurePoint.add(String.join(":", list));
    }

    return normalizedMeasurePoint.toString();
  }

  private static String normalizeBase(String restUrlMeasurePoint) {
    if (!restUrlMeasurePoint.contains("/")) {
      return restUrlMeasurePoint;
    }

    // general replacements of search terms and other not easy matchable values like sessionId
    restUrlMeasurePoint = nameRegex.matcher(restUrlMeasurePoint).replaceAll("name=-generic-Name-");
    restUrlMeasurePoint =
        prefixRegex.matcher(restUrlMeasurePoint).replaceAll("prefix=-generic-Prefix-");
    restUrlMeasurePoint =
        sessionidRegex.matcher(restUrlMeasurePoint).replaceAll("sessionid=-generic-SessionId-");
    restUrlMeasurePoint =
        jwtRegex.matcher(restUrlMeasurePoint).replaceAll("$1-jwt-token-");
    restUrlMeasurePoint =
        jwtHeaderRegex.matcher(restUrlMeasurePoint).replaceAll("$1-jwt-header-");

    final String[] parts =
        restUrlMeasurePoint
            .replaceAll("//", "/")
            .replaceAll("\\{", "")
            .replaceAll("}", "")
            .replaceAll("\\[", "")
            .replaceAll("]", "")
            .replace("u0026", "&")
            .replace("%26", "&")
            .split(" ");
    UriComponentsBuilder builder = null;
    try {
      builder = UriComponentsBuilder.fromUri(new URI(parts[0]));
    } catch (final URISyntaxException e) {
      log.warn(String.format("invalid uri %s", restUrlMeasurePoint), e);
      return restUrlMeasurePoint;
    }
    final UriComponents components = builder.build();
    parts[0] = processTokens(components.getPath(), "/");

    // process query parameters

    final List<String> params = new ArrayList<>();
    components
        .getQueryParams()
        .forEach(
            (name, values) -> {
              values.forEach(
                  value -> {
                    params.add(name + "=" + matchToken(value));
                  });
            });

    if (components.getQueryParams().size() > 0) {
      parts[0] += "?" + String.join("&", params);
    }
    return String.join(" ", parts);
  }

  private static String processTokens(final String input, final String delim) {
    final String[] tokens = input.split(delim);
    int i = 0;
    for (final String token : tokens) {
      if (token.contains(";")) {
        tokens[i++] = processTokens(token, ";");
      } else if (token.contains("=")) {
        tokens[i++] = processTokens(token, "=");
      } else if (token.contains(",")) {
        tokens[i++] = processTokens(token, ",");
      } else {
        tokens[i++] = matchToken(token);
      }
    }
    return String.join(delim, tokens);
  }

  private static String matchToken(final String token) {
    // skip text only
    if (token == null || az.matcher(token).matches()) {
      return token;
    }
    for (final Map.Entry<String, Pattern> entry : patterns.entrySet()) {
      final Pattern pattern = entry.getValue();
      final String replacement = entry.getKey();
      final String newToken = pattern.matcher(token).replaceAll(replacement);
      if (!token.equals(newToken)) {
        return newToken;
      }
    }

    return token;
  }

  // special replacements of urls
  @Builder(toBuilder = true)
  static class SpecialReplacement {

    String startsWith;
    String contains;
    Pattern matchPattern;
    String replacement;
    String sampleInput;
    String expectedResult;
  }
}
