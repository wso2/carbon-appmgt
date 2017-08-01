(function(){
    
	var log = new Log();

    var sso_sessions = application.get('sso_sessions');
    
    // 'sso_sessions' property is availabe only after the user signs in.
    // So we should proceed only if 'sso_sessions' is available.
    var sessionIndex = session.get("sessionIndex");
    if (sso_sessions) {
        if (sessionIndex) {
            var httpSessionForSessionIndex = sso_sessions[sessionIndex];
            if (httpSessionForSessionIndex) {
                if (log.isDebugEnabled()) {
                    log.debug("Deleting SSO session " + sessionIndex + " for the HTTP session " +
                              httpSessionForSessionIndex.getId());
                }
                delete sso_sessions[sessionIndex];
            }
    	}
    }

}());