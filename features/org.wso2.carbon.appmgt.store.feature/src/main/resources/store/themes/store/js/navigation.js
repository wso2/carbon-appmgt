$(function() {

	var showError = function (message) {
		var msg = message.replace(/[0-9a-z.+]+:\s/i, '');
		$('#register-alert').html(msg).fadeIn('fast');
		$('#btn-signin').text('Sign In').removeClass('disabled');
	};

    var showLoginError = function (message) {
        var msg = message.replace(/[0-9a-z.+]+:\s/i, '');
        $('#login-alert').html(msg).fadeIn('fast');
        $('#btn-signin').text('Sign In').removeClass('disabled');
    };

    var login = function() {
		if (!$("#form-login").valid())
			return;
		$('#btn-signin').addClass('disabled').text('Signing In');

		var username = $('#inp-username').val();
		var password = $('#inp-password').val();

        	caramel.ajax({
                type: 'POST',
                url: '/apis/user/login',
                data: JSON.stringify({
                    username: username,
                    password: password
                }),
                success: function (data) {
                    if (!data.error) {
                        var assetId = $('#modal-login').data('value');
                        if (assetId == "" || assetId == null) {
                            location.reload();
                        } else {
                            window.location = caramel.tenantedUrl('/assets/webapp/' + assetId);
                        }
                    } else {
                        showLoginError(data.message);
                    }
                },
                contentType: 'application/json',
                dataType: 'json'
        	});
 	};
    var validateUsername = function(username) {

        var errorMessage = "";
        //check for username policy requirements
        if(!username.match(/^[\S]{3,30}$/)){
            errorMessage = "No conformance";
            return errorMessage;
        }
        if (username == '') {
            errorMessage = "Empty string";
            return errorMessage;
        }
        if(username.indexOf("/") > -1){
            errorMessage = "Domain";
            return errorMessage;
        }
        return errorMessage;
    }

    var validatePassword = function(pw1, pw2) {
        var error = "";

        // check for a value in both fields.
        if (pw1 == '' || pw2 == '') {
            error = "Empty Password";
            return error;
        }
        //check for password policy requirements
        if (!pw1.match(/^[\S]{5,30}$/)) {
            error = "No conformance";
            return error;
        }
        //check the typed passwords mismatch
        if (pw1 != pw2) {
            error = "Password Mismatch";
            return error;
        }
        return error;
    }

    var isEnableEmailUsername = function () {
        var isEmailUsername = false;
        caramel.ajax({
            type: 'POST',
            url: '/apis/user/emailLogin',
            async: false,
            success: function (data) {
                isEmailUsername = data.isEmailUsername
                return isEmailUsername;
            },
            contentType: 'application/json',
            dataType: 'json'
        });
        return isEmailUsername;
    }

    var doValidation = function(usename, pw1, pw2) {
        if (!isEnableEmailUsername()) {
            if (usename.indexOf("@") != -1) {
                showError("Username cannot contain @ sign.");
                return;
            }
        }

        var reason = "";
        reason = validateUsername(usename);
        if (reason != "") {
            if (reason == "No conformance") {
                showError("Entered user name is not conforming to policy. Please enter a user name of 3-30 characters without any whitespaces.")
            } else if (reason == "Empty string") {
                showError("Entered user name is empty. Please enter a valid user name.");
            } else if (reason == "Domain") {
                showError("Entered user name contains a domain. Please enter a valid user name with out a domain.");
            }
            return false;
        }
        reason = validatePassword(pw1, pw2);
        if (reason != "") {
            if (reason == "Empty Password") {
                showError("Password or Password Repeat fields can not be empty.");
            } else if (reason == "Password Mismatch") {
                showError("Password and Password Repeat do not match. Please re-enter.");
            } else if (reason == "No conformance") {
                showError("Invalid Password! Please enter a password of 5-30 characters without any whitespaces.")
            }
            return false;
        }
        return true;
    }

	var register = function() {


		if (!$("#form-register").valid())
			return;

		var username = $('#inp-username-register').val();
		var password = $('#inp-password-register').val();
		var confirmPassword = $('#inp-password-confirm').val();




        if(!doValidation(username,password,confirmPassword))
            return;

        caramel.ajax({
            type: 'POST',
            url: '/apis/user/register',
            data: JSON.stringify({
                username : username,
                password : password
            }),
            success: function (data) {

                if (!data.error) {
                    var messageText = null;

                    //If userSignUp workflow is enabled
                    if(data.showWorkflowMsg){
                        messageText = "User account awaiting Administrator approval.";

                    }else{
                        messageText = "Account for user '"+username+"' created successfully. You can now signin to " +
                            "APP store using login name '"+username+"'.";
                    }

                    $('#register-modal').modal('hide');
                    noty({
                        text : messageText,
                        'layout' : 'center',
                        'timeout': 3000,
                        'modal': true,
                        'onClose': function() {

                            location.href = caramel.tenantedUrl("/login");
                        }
                    });


                } else {
                    showError(data.message);
                }
            },
            contentType: 'application/json',
            dataType: 'json'
        });

	};

	$('#btn-signout').on('click', function() {
		caramel.post("/apis/user/logout", function(data) {
			location.reload();
		}, "json");
	});

	$('#btn-signin').bind('click', login);

	$('#modal-login input').bind('keypress', function(e) {
		if (e.keyCode === 13) {
			login();
		}
	});

	$('#btn-register-submit').click(register);



	$('#modal-register input').keypress(function(e) {
		if (e.keyCode === 13) {
			register();
		}
	});


	$('#sso-login').click(function() {
		$('#sso-login-form').submit();
	});

	$('.store-menu > li > a').click(function(){
		var url = $(this).attr('href');
		window.location = url;
	});

    	$('.store-menu > li > ul > li > a').click(function(){
        	var url = $(this).attr('href');
        	window.location = url;
    	});




    $('#btn-register-close').click(function () {
        clearFields();
    });

});


//clear input fields
function clearFields() {
    $('#inp-username-register').val(""); //clear username field
    $('#inp-password-register').val(""); //clear password field
    $('#inp-password-confirm').val(""); //clear confirm password field
    $('#register-alert').hide(); //hide validation messages
}
