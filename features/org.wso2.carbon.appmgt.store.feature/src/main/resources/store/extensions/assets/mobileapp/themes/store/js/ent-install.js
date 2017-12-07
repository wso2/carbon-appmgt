var selectedTab = "roles";

var selectedApp = "";


$(document).ready( function () {


if($('#isEnterpriseInstallEnabled').val() === 'true'){
    oTable = $('#roles-table').dataTable({
        "sDom": "<'row-fluid'<'tabel-filter-group span8'T><'span4'>r>t<'row-fluid'<'span6'i><'span6'p>>",
        "aaSorting": [
            [ 0, "desc" ]
        ],
        "iDisplayLength": 5,
        "bStateSave": false,
        "sAjaxSource": caramel.context + "/apis/enterprise/get-all-roles",
        "oTableTools": {
            "aButtons": [
                "copy",
                "print",
                {
                    "sExtends": "collection",
                    "sButtonText": 'Save <span class="caret" />',
                    "aButtons": [ "csv", "xls", "pdf" ]
                }
            ]
        },

        aoColumns: [

            {   "mData": function (data, type, full) {
                return  data[0];
            }
            },
            {   "mData": function (data, type, full) {
                return '<input type="checkbox"  class="select-checkbox role-checkbox" value="' + data[0] + '" id="' + data[0] + '"  data-true-val="true" data-false-val="false">';
            }
            }
        ]
    });


    oTableUsers = $('#users-table').dataTable({
        "sDom": "<'row-fluid'<'tabel-filter-group span8'T><'span4'>r>t<'row-fluid'<'span6'i><'span6'p>>",
        "aaSorting": [
            [ 0, "desc" ]
        ],
        "iDisplayLength": 5,
        "bStateSave": false,
        "sAjaxSource": caramel.context + "/apis/enterprise/get-all-users",
        "oTableTools": {
            "aButtons": [
                "copy",
                "print",
                {
                    "sExtends": "collection",
                    "sButtonText": 'Save <span class="caret" />',
                    "aButtons": [ "csv", "xls", "pdf" ]
                }
            ]
        },

        aoColumns: [
            {   "mData": function (data, type, full) {
                return  data[0];
            }
            },
            {   "mData": function (data, type, full) {
                return '<input type="checkbox"  class="select-checkbox user-checkbox" value="' + data[0] + '" id="' + data[0] + '"  data-true-val="true" data-false-val="false">';
            }
            }
        ]
    });

}


    $("#btn-apps-ent-install").click(function () {

            if ($('#chkRemovable').prop('checked') == true) {
               isRemovable = true;
            } else {
               isRemovable = false;
            }

            if(selectedTab === "roles"){
                installToRoles();
            }else if (selectedTab === "users"){
                installToUsers();
            }
    });



    $("#btn-ent-install").click(function () {
        selectedApp = $(this).data("aid");
    });


    $("#btn-apps-ent-uninstall").click(function () {



            if(selectedTab === "roles"){
                unInstallFromRoles();
            }else if (selectedTab === "users"){
                unInstallFromUsers()
            }
    });



    $("#btn-ent-install-close").click(function () {
        clearSelected();
    });

    $("#roleId").keyup(function(e){
        searchRoleByName();
    });

    $("#userId").keyup(function(e){
        searchUserByName();
    });

    function searchRoleByName() {
        var roleId = $('#roleId').val();
        var url = '/store/apis/enterprise/get-roles/' + $('#roleId').val();
        if(roleId === ""){
            url = '/store/apis/enterprise/get-all-roles/';
        }
        
        $.getJSON(url, null, function(json) {
            table = $('#roles-table').dataTable();
            oSettings = table.fnSettings();
            table.fnClearTable(this);
            table.fnAddData(json.aaData);

            oSettings.aiDisplay = oSettings.aiDisplayMaster.slice();
            table.fnDraw();
        });
    }

    function searchUserByName() {
        var userId = $('#userId').val();
        var url = '/store/apis/enterprise/get-users/' + $('#userId').val();
        if(userId === ""){
            url = '/store/apis/enterprise/get-all-users/';
        }

        $.getJSON(url, null, function(json) {
            table = $('#users-table').dataTable();
            oSettings = table.fnSettings();
            table.fnClearTable(this);
            table.fnAddData(json.aaData);

            oSettings.aiDisplay = oSettings.aiDisplayMaster.slice();
            table.fnDraw();
        });
    }



    function clearSelected(){
        $(".role-checkbox:checked", oTable.fnGetNodes()).each(function(){
            $(this).removeAttr( 'checked' );
        });

        $(".user-checkbox:checked", oTableUsers.fnGetNodes()).each(function(){
            $(this).removeAttr( 'checked' );
        });
    }

    $('#ent-install-tabs a').click(function (e) {
        e.preventDefault();
        clearSelected();
        selectedTab = $(this).data("name");
        $(this).tab('show');

    })


    function installToRoles(){

        var rolesSelected = [];
        $(".role-checkbox:checked", oTable.fnGetNodes()).each(function(){
            rolesSelected.push($(this).val());
        });



        if(rolesSelected.length > 0){

            var rolesString = "";
            for(i = 0; i < rolesSelected.length; i++){
                rolesString += rolesSelected[i];
                if(i < (rolesSelected.length - 1)){
                    rolesString += ", ";
                }
            }

            var manyOne = rolesSelected.length > 1 ? 's' : '';
            $('#modalEnterpriseInstall').modal('hide');
            noty({
                text : 'Are you sure you want to install this app to "' + rolesString + '" role' +  manyOne +'?',
                'layout' : 'center',
                'modal' : true,
                buttons : [{
                    addClass : 'btn',
                    text : 'Yes',
                    onClick : function($noty) {

                        $noty.close();


                        $.ajax({
                            type: "POST",
                            url: caramel.context + "/apis/enterprise/perform/install/role",
                            data: { app: selectedApp, data:  rolesSelected, removable: isRemovable }
                        })
                            .done(function( msg ) {
                                noty({
                                    text : 'App is installed and subscribed to the selected role',
                                    'layout' : 'center',
                                    'modal' : true,
                                    timeout: 1000,
                                    'onClose': function() {
                                        location.reload();
                                    }
                                });
                        });

                    }
                },
                    {
                        addClass : 'btn',
                        text : 'No',
                        onClick : function($noty) {
                            $noty.close();
                            $('#modalEnterpriseInstall').modal('show');
                        }
                    }]
            });
        }else{
            noty({
                text : 'No role is selected',
                'layout' : 'center',
                'type' : 'error',
                'modal' : true,
                timeout: 1000
            });
            $('#modalEnterpriseInstall').modal('show');
        }

    }

    function unInstallFromRoles(){

        var rolesSelected = [];
        $(".role-checkbox:checked", oTable.fnGetNodes()).each(function(){
            rolesSelected.push($(this).val());
        });

        if(rolesSelected.length > 0){

            var rolesString = "";
            for(i = 0; i < rolesSelected.length; i++){
                rolesString += rolesSelected[i];
                if(i < (rolesSelected.length - 1)){
                    rolesString += ", ";
                }
            }

            var manyOne = rolesSelected.length > 1 ? 's' : '';
            $('#modalEnterpriseInstall').modal('hide');
            noty({
                text : 'Are you sure you want to uninstall this app from "' + rolesString + '" role' +  manyOne +'?',
                'layout' : 'center',
                'modal' : true,
                buttons : [{
                    addClass : 'btn',
                    text : 'Yes',
                    onClick : function($noty) {

                        $noty.close();

                        $.ajax({
                            type: "POST",
                            url: caramel.context + "/apis/enterprise/perform/uninstall/role",
                            data: { app: selectedApp, data:  rolesSelected }
                        })
                            .done(function( msg ) {
                                noty({
                                    text : 'App is uninstalled and unsubscribed from the selected role',
                                    'layout' : 'center',
                                    'modal' : true,
                                    timeout: 1000,
                                    'onClose': function() {
                                        location.reload();
                                    }
                                });
                         });

                    }
                },
                    {
                        addClass : 'btn',
                        text : 'No',
                        onClick : function($noty) {
                            $noty.close();
                            $('#modalEnterpriseInstall').modal('show');
                        }
                    }]
            });
        }else{
            noty({
                text : 'No role is selected',
                'layout' : 'center',
                'type' : 'error',
                'modal' : true,
                timeout: 1000
            });
        }

    }

    function installToUsers(){

        var usersSelected = [];
        $(".user-checkbox:checked", oTableUsers.fnGetNodes()).each(function(){
            usersSelected.push($(this).val());
        });

        if(usersSelected.length > 0){

            var usersString = "";
            for(i = 0; i < usersSelected.length; i++){
                usersString += usersSelected[i];
                if(i < (usersSelected.length - 1)){
                    usersString += ", ";
                }
            }

            var manyOne = usersSelected.length > 1 ? 's' : '';
            $('#modalEnterpriseInstall').modal('hide');
            noty({
                text : 'Are you sure you want to install this app to "' + usersString + '" user' +  manyOne +'?',
                'layout' : 'center',
                'modal' : true,
                buttons : [{
                    addClass : 'btn',
                    text : 'Yes',
                    onClick : function($noty) {

                        $noty.close();

                        $.ajax({
                            type: "POST",
                            url: caramel.context + "/apis/enterprise/perform/install/user",
                            data: { app: selectedApp, data:  usersSelected, removable: isRemovable }
                        })
                            .done(function( msg ) {
                                noty({
                                    text : 'App is installed and subscribed to the selected user',
                                    'layout' : 'center',
                                    'modal' : true,
                                    timeout: 1000
                                });
                         });

                    }
                },
                    {
                        addClass : 'btn',
                        text : 'No',
                        onClick : function($noty) {
                            $noty.close();
                            $('#modalEnterpriseInstall').modal('show')
                        }
                    }]
            });
        }else{
            noty({
                text : 'No user is selected',
                'layout' : 'center',
                'type' : 'error',
                'modal' : true,
                timeout: 1000
            });
        }

    }

    function unInstallFromUsers(){


        var usersSelected = [];
        $(".user-checkbox:checked", oTableUsers.fnGetNodes()).each(function(){
            usersSelected.push($(this).val());
        });

        if(usersSelected.length > 0){

            var usersString = "";
            for(i = 0; i < usersSelected.length; i++){
                usersString += usersSelected[i];
                if(i < (usersSelected.length - 1)){
                    usersString += ", ";
                }
            }

            var manyOne = usersSelected.length > 1 ? 's' : '';
            $('#modalEnterpriseInstall').modal('hide');
            noty({
                text : 'Are you sure you want to uninstall this app from "' + usersString + '" user' +  manyOne +'?',
                'layout' : 'center',
                'modal' : true,
                buttons : [{
                    addClass : 'btn',
                    text : 'Yes',
                    onClick : function($noty) {

                        $noty.close();

                        $.ajax({
                            type: "POST",
                            url: caramel.context + "/apis/enterprise/perform/uninstall/user",
                            data: { app: selectedApp, data:  usersSelected }
                        })
                            .done(function( msg ) {
                                noty({
                                    text : 'App is uninstalled and unsubscribed from the selected user',
                                    'layout' : 'center',
                                    'modal' : true,
                                    timeout: 1000
                                });
                            });

                    }
                },
                    {
                        addClass : 'btn',
                        text : 'No',
                        onClick : function($noty) {
                            $noty.close();
                            $('#modalEnterpriseInstall').modal('show');
                        }
                    }]
            });
        }else{
            noty({
                text : 'No user is selected',
                'layout' : 'center',
                'type' : 'error',
                'modal' : true,
                timeout: 1000
            });
        }

    }


} );


