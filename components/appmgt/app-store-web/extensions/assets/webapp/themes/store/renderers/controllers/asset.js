var render = function(theme, data, meta, require) {

	var log = new Log();
	
	var apiUtil = Packages.org.wso2.carbon.appmgt.impl.utils.APIUtil;
	var gateway = apiUtil.getGatewayendpoints();

	gateway = gateway.split(",");
	var gtw = {};

	for each(var url in gateway){
		if(url.indexOf("https") > -1){
			gtw['https'] = url;
		}else{
			gtw['http'] = url;
		}
	}


	log.debug(gtw);

	theme('2-column-right', {
		title : data.title,
		
		header: [
					{
						partial: 'header',
						context: data.header
					}
				],
				navigation: [
					{
						partial: 'navigation',
						context: require('/helpers/navigation.js').currentPage(data.navigation, data.type, data.search)
					}
				],
		
        body : [{
            partial : 'asset',
            context : require('/helpers/asset.js').format({
                user : data.user,
                sso : data.sso,
                asset : data.asset,
                type : data.type,
                inDashboard : data.inDashboard,
                embedURL : data.embedURL,
                isSocial : data.isSocial,
		gateway : gtw
            })
        }],
		right : [
			{
                partial: 'my-assets-link',
                context: data.myAssets
            },
			{
                partial: 'recent-assets',
                context: require('/helpers/asset.js').formatRatings(data.recentAssets)
            }, {
				partial : 'tags',
				context : data.tags
			}
		]
	});
};
