var checkeRole = function (username, session) {
	var opts, um,role
      	authorized = false,
      	carbon = require('carbon'),
      	event = require('event'),
      	usr = carbon.server.tenantUser(username);
	var log = new Log();

    var config = require('/config/store.json');

	var user = require('store').user;
	if (!user.configs(usr.tenantId)) {
		event.emit('tenantLoad', usr.tenantId);
	}

	opts = user.configs(usr.tenantId);
	role = opts.userRoles;

	var server=require('store').server;
  	um = server.userManager(usr.tenantId);
  	usr = um.getUser(usr.username);
  	usr.tenantDomain = carbon.server.tenantDomain({tenantId: usr.tenantId});


    if (usr.tenantId != -1234) {
        usr.username += "@" + usr.tenantDomain;
    }
    event.emit('login', usr.tenantId, usr, session);
    return true;
};
