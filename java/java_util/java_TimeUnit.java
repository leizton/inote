HttpSession session = request.getSession(true);
session.setMaxInactiveInterval( (int) TimeUnit.DAYS.toSeconds(1) );  // 不要用24*3600这样的魔数