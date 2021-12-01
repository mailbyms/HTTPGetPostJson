import org.apache.commons.codec.digest.DigestUtils;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.httpclient.ResponseInputStream.LOG;

public class HttpGetPostJsonMain {

    public static void main(String[] args) {

        String url = "http://192.168.1.70:8086";
        String modelFunction = "/user/sendVerificationCode2";

        String mobile = "18312060683";
        long time = new Date().getTime();
        int type = 0;
        String signature = DigestUtils.md5Hex(time + "H&*" + mobile + type);


        Map<String, String> modelArgs = new HashMap<>();
        modelArgs.put("type", String.valueOf(type));
        modelArgs.put("mobile", mobile);
        modelArgs.put("time", String.valueOf(time));
        modelArgs.put("signature", signature);

        try {
            URL u = new URL(url + "/" + modelFunction);


            //String results = RESTUtil.INSTANCE.getRESTJSONResults(u, modelArgs);

            String results = RESTUtil.INSTANCE.postRESTJSONResults(u, modelArgs);

            LOG.info("result: " + results);

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);

        }

    }
}
