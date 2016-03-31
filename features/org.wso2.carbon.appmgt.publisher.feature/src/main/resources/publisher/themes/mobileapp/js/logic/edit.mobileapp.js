var appMetaData = null;

$('#application-tab a').click(function(e) {
	e.preventDefault();
	$(this).tab('show');
});


$('#txtVisibility').tokenInput(caramel.context +'/api/lifecycle/information/meta/webapp/roles', {
	theme: 'facebook',
	preventDuplicates: true,
	hintText: "Type in a user role"
});

function populateVisibleRoles(){

	var visibilityComponent = $('#txtVisibility');
	var visibleRoles = visibilityComponent.data('roles');

	if(visibleRoles){
		visibleRoles = visibleRoles.split(",");

		for(var i = 0; i < visibleRoles.length; i++){
			var role = visibleRoles[i];
			visibilityComponent.tokenInput("add", {id: role, name: role});
		}
	}

}

populateVisibleRoles();
 
 
$('#txtOS').on("change",function() {
	  if($('#txtOS').val() == 'webapp'){
		  $('#control-webapp').show();
		  $('#app-upload-block').css('display', 'none');
		  $('#market-type-block').css('display', 'none');
		  
	  }else{
		  $('#control-webapp').hide();
		   $('#app-upload-block').css('display', 'block');
		   $('#market-type-block').css('display', 'block');
	  }

	  
	   if($('#txtOS').val() == 'android'){
	   		$('#txtNameLabel').text('Package Name');
		   	if($('#txtMarket').val() == "VPP"){
		   		$('#txtMarket').val('Market');
		   	}
	   		$('#file-upload-text').html('<i class="icon-plus-sign"></i> SELECT .APK FILE');
	   		$('#txtMarket').children('option[value="VPP"]').css('display','none');
	   		
	   }else if($('#txtOS').val() == 'ios'){
	   		$('#txtNameLabel').text('App Identifier');
	   		$('#file-upload-text').html('<i class="icon-plus-sign"></i> SELECT .IPA FILE');
	   		$('#txtMarket').children('option[value="VPP"]').css('display','block');   		
	   }
	  
	  
});


$('#txtMarket').on("change",function() {
	  if($('#txtMarket').val() == 'Market' || $('#txtMarket').val() == 'VPP'){
		  $('#file-upload-block').css('display', 'none');
		  $('#package-select-block').css('display', 'block');
	  }else{
		  $('#package-select-block').css('display', 'none');
		  $('#file-upload-block').css('display', 'block');
	  }
	  
	   if($('#txtOS').val() == 'android'){
	   		$('#file-upload-text').txt('SELECT .APK FILE');
	   }else if($('#txtOS').val() == 'android'){
	   		$('#file-upload-text').txt('SELECT .PLIST FILE');
	   }
});


	 
	    $(document).ready(function() { 
	    	
            // bind 'myForm' and provide a simple callback function 
            $('#form-asset-create').ajaxForm(function(data) { 
            
            	try{
                    data = (typeof data == "string") ? JSON.parse(data) : data;
            	}catch(e){
            		window.location.replace(caramel.context +"/assets/mobileapp/");
               		return;
            	}
               
               	if(data.ok == false){
               		
               		var validationErrors = "";
               		
               		for (var key in data.report) {
					  if (data.report.hasOwnProperty(key)) {					   
					    if(key != "failed"){
					    	validationErrors += data.report[key] + "<br>";
					    }
					    
					  }
					}
               		
               		 noty({               		 	
					    text: '<strong>Validation Failed!</strong> <br />' + validationErrors,
					    template: '<div class="noty_message"><span class="noty_text"></span><div class="noty_close"></div></div>',
					    layout: "center",
					    timeout: 2000,
					    type: "error"
				   
				 	 });
               		
               		
               	}else{
               		window.location.replace(caramel.context +"/assets/mobileapp/");
               	}
               
				
             
            });


        });
	





$('#btn-create-asset-mobile').click(function(e) {
	
	var name = $("#txtName").val();
	var description = $("#txtDescription").val();
	var category = $("#txtCategory").val();
	var recentChanges = $("#txtRecentChanges").val();
	var banner = $("#txtbanner").val();
	var screenShot1 = $("#txtScreenShot1").val();
	var screenShot2 = $("#txtScreenShot2").val();
	var screenShot3 = $("#txtScreenShot3").val();	
	var iconfile = $("#txtIconfile").val();
	var isMeetGudeLines = $("#chkMeetGudeLines").val();
	
	var params = {
        name: name,
        description: description,
        category: category,
        recentChanges: recentChanges,
        banner: banner,
        screenShot1: screenShot1,
        screenShot2: screenShot2,
        screenShot3: screenShot3,
        iconfile: iconfile,
        isMeetGudeLines: isMeetGudeLines,
        url: "downloads/agent.apk",
        provider: "wso2",
        version: "1.0",
        metadata : appMetaData	
     };
	
	
	$.ajax({
      type: "POST",
      url: caramel.context +"/api/asset/mobileapp",
      contentType: "application/json",
      data: JSON.stringify(params),
      success: function () {
        alert("Data Uploaded: ");
      }
    });
});


$('#modal-upload-app').on('shown', function() {
        $(".dropdownimage").msDropDown();
});





$('#btn-app-upload').click(function () {
		          if(appMetaData == null){
		          	  $("#modal-upload-data").css("display", "none");
		              $('#modal-upload-app').modal('hide');
		          }		                  
		                                      
});


jQuery("#form-asset-create").submit(function(e) {
	//e.preventDefault();
   //alert($('#appmeta').val());
});

$( document ).ajaxComplete(function( event, xhr, settings ) {
   if(xhr.status == 401){
       location.re
   }
});

/*=======Fetch the tags and populate the tags field=====*/
var url = window.location.pathname;
//Break the url into components
var comps = url.split('/');

//Given a url of the form /pub/api/asset/{asset-type}/{asset-id}
//length=5
//then: length-2 = {asset-type} length-1 = {asset-id}
var assetId = comps[comps.length - 1];
var assetType = comps[comps.length - 2];

fetchTagsOfType(assetType);


/*
 The function is used to fetch the tags of the current asset type
 @assetType: The asset type for which the tags must be fetched
 */
function fetchTagsOfType(assetType) {

    $.ajax({
               url: caramel.context + '/api/tag/' + assetType,
               type: 'GET',
               success: function (response) {
                   var tags = response;
                   fetchTagsOfAsset(assetType, assetId, tags);
               },
               error: function () {
                   console.log('unable to retrieve tags for ' + assetType);
               }
           });
}


/*
 The function is used to fetch the tags of the current asset
 @assetType: The asset type for which the tags must be fetched
 @assetId: The asset id of the asset for which the tags must be fetched
 */
function fetchTagsOfAsset(assetType, assetId, masterTags) {

    $.ajax({
               url: caramel.context + '/api/tag/' + assetType + '/' + assetId,
               type: 'GET',
               success: function (response) {
                   //Initialize the tag container
                   $('#txtTags').tokenInput(masterTags, {
                       theme: "facebook",
                       prePopulate: response,
                       allowFreeTagging: true
                   });
               }
           });

}

//obtain the selected tags{id,value} and send tags values in the form submission
$('#submitButton').click(function(){
    var selectedTags = $('#txtTags').tokenInput('get');
    var tags = [];
    for (var index in selectedTags) {
        tags.push(selectedTags[index].name);
    }
    $('#txtTags').val(tags);
});

/*===============================================================================*/
//prevent form submission by pressing enter key
//because when adding tags need to press the etner key to add the tags
$(document).on("keypress", "form", function(event) {
    return event.keyCode != 13;
});