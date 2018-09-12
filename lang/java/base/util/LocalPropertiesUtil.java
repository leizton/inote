public class LocalPropertiesUtil {
  private static final Logger LOG = LoggerFactory.getLogger(LocalPropertiesUtil.class);

  @SuppressWarnings("unchecked")
  public static Map<String, String> loadMap(String filename, boolean ignoreFail) throws Exception {
    return new HashMap<String, String>((Map) loadProp(filename, ignoreFail));
  }

  public static Properties loadProp(String filename, boolean ignoreFail) throws Exception {
    try {
      return doLoadProp(filename);
    } catch (Exception e) {
      if (ignoreFail) {
        LOG.info("load properties exception: {}", filename, e);
        return new Properties();
      } else {
        throw e;
      }
    }
  }

  private static Properties doLoadProp(String filename) throws IOException, URISyntaxException {
    InputStream inp = null;
    try {
      inp = getResourceInputStream(filename);
      Properties conf = new Properties();
      conf.load(inp);
      LOG.info("load local properties: {}", filename);
      return conf;
    } finally {
      if (inp != null) {
        try {
          inp.close();
        } catch (IOException e) {
          LOG.error("close exception: {}", filename, e);
        }
      }
    }
  }

  private static InputStream getResourceInputStream(String resourceName) {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    if (loader == null) {
      loader = LocalPropertiesUtil.class.getClassLoader();
    }
    // jar包里的a.conf, 用loader.getResource()得到的url是file://xxx.jar!/a.conf
    // 路径的jar!不能直接用new File打开, 所以改用getResourceAsStream()
    return loader != null ? loader.getResourceAsStream(resourceName) : null;
  }
}