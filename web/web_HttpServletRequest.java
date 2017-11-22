HttpServletRequest
	是一个接口.
	extends ServletRequest
接口
	getSession():HttpSession


HttpServletRequestWrapper
	是HttpServletRequest的一个实现类.
	extends ServletRequestWrapper implements HttpServletRequest
构造器
	HttpServletRequestWrapper(HttpServletRequest request) {
		// request是org.apache.catalina.connector.Request的实例
		super(request)  // ServletRequestWrapper里会存下request
	}
方法
	// 获取Session
	getSession(boolean create):HttpSession @Override {
		return this._getHttpServletRequest().getSession(create)
	}
	getSession():HttpSession @Override {
		return this._getHttpServletRequest().getSession()
	}
	_getHttpServletRequest():HttpServletRequest {
		return (HttpServletRequest) super.getRequest()
	}


Request
	是HttpServletRequest的实现类.
	implements HttpServletRequest
方法
	getSession(boolean create):HttpSession @Override {
		Session session = doGetSession(create)
		return session != null
			   ? session.getSession() : null
	}
	doGetSession(boolean create):Session {
		Context context = getContext()
		if context == null
			retur null

		// 尝试返回当前session
		// session.isValid() == true 有效, 未过期
		if session != null && session.isValid()
			return session
		session = null

		// 尝试从Manager中查找session
		Manager manager = context.getManager()
		if manager == null
			return null  // 不支持session
		if requestedSessionId != null
			session = manager.findSession(requestedSessionId)  // findSession
			if session != null && session.isValid()
				session.access()  // 更新访问时间, 防止过期
				return session
			session = null

		// 创建新的session
		if !create
			return null
		if response != null
		   && context.getServletContext()
		   			.getEffectiveSessionTrackingModes()
					.contains(SessionTrackingMode.COOKIE)
		   && response.getResponse().isCommitted()
			throw new IllegalStateException(sm.getString("coyoteRequest.sessionCreateCommitted"))
		String sessionId = getRequestedSessionId()  // 重用sessionId
		if requestedSessionSSL
			// 从SSL handshake获取sessionId
		else if context.getSessionCookiePath().equals("/")
				&& context.getValidateClientProvidedNewSessionId()
				boolean found = false
				for Container container : getHost().findChildren()
					Manager m = ((Context) container).getManager()
					if m != null
						if m.findSession(sessionId) != null
							found = true
							break
				if !found
					sessionId = null
		else
			sessionId = null
		session = manager.createSession(sessionId);

		// 添加session对应的cookie
		if session != null
		   && context.getServletContext()
					.getEffectiveSessionTrackingModes()
					.contains(SessionTrackingMode.COOKIE)
			Cookie cookie =
				ApplicationSessionCookieConfig.createSessionCookie(
						context, session.getIdInternal(), isSecure())
			response.addSessionCookieInternal(cookie)

		// 返回session
		if session == null
			return null
		session.access()
		return session
	}