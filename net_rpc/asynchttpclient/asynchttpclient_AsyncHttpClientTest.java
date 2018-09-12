public class AsyncHttpClientTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncHttpClientTest.class);

    @Test
    public void testConfigDefaults() {
        LOGGER.info("默认连接超时: {}毫秒", AsyncHttpClientConfigDefaults.defaultConnectTimeout());
        LOGGER.info("默认连接超时: {}毫秒", AsyncHttpClientConfigDefaults.defaultRequestTimeout());
    }

    @Test
    public void testRequest() throws Exception {
        // config，用于所有request
        DefaultAsyncHttpClientConfig.Builder clientConfig = new DefaultAsyncHttpClientConfig.Builder()
                .setConnectTimeout(10000);

        // request
        Request request = new RequestBuilder()
                .setUri(new Uri("http", null, "fanyi.baidu.com", 80, "/v2transapi", "query=异步")).build();

        // client
        DefaultAsyncHttpClient client = new DefaultAsyncHttpClient(clientConfig.build());

        // execute request
        client.executeRequest(request,
            // 异步处理
            // @see org.asynchttpclient.AsyncCompletionHandler
            new AsyncHandler<Object>() {
            public State onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
                LOGGER.info("status: {}", responseStatus.getStatusCode());
                return State.CONTINUE;
            }

            public State onHeadersReceived(HttpResponseHeaders headers) throws Exception {
                headers.getHeaders().forEach(
                        header -> LOGGER.info("header, key: {}, value: {}", header.getKey(), header.getValue())
                );
                return State.CONTINUE;
            }

            public State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
                bodyParts.add(bodyPart);
                LOGGER.info("get body part {}, length: {}", bodyParts.size(), bodyPart.length());
                if (bodyPart.isLast()) {
                    handle();
                }
                return State.CONTINUE;
            }

            public Object onCompleted() throws Exception {
				// AsyncCompletionHandlerBase 在此处返回 Response
                return null;
            }

            public void onThrowable(Throwable t) {
                LOGGER.error("请求失败, address: {}, url: {}", request.getAddress(), request.getUrl(), t);
            }

            private List<HttpResponseBodyPart> bodyParts = Lists.newArrayList();

            private void handle() {
                StringBuilder buf = new StringBuilder();
                bodyParts.forEach(bodyPart -> {
                    try {
                        buf.append(new String(bodyPart.getBodyPartBytes(), "utf-8"));
                    } catch (UnsupportedEncodingException e) {
                        LOGGER.error("编码字符集设置错误", e);
                    }
                });
                LOGGER.info("response: {}", buf.toString());
            }
        }).get(10, TimeUnit.SECONDS);
    }

    @Test
    public void testRequestV2() throws Exception {
        // config，用于所有request
        DefaultAsyncHttpClientConfig.Builder clientConfig = new DefaultAsyncHttpClientConfig.Builder()
                .setConnectTimeout(10000);

        // request
        Request request = new RequestBuilder()
                .setUri(new Uri("http", null, "fanyi.baidu.com", 80, "/v2transapi", "query=异步")).build();

        // response
        Response response = new DefaultAsyncHttpClient(clientConfig.build())
                .executeRequest(request).get(10, TimeUnit.SECONDS);
        LOGGER.info("response: {}", response.getResponseBody());
    }
}
