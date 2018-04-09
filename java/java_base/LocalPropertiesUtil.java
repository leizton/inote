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
    return loader != null ? loader.getResourceAsStream(resourceName) : null;
  }
}