function transform(businessOwner){
    var owner = [];
    owner.push({
                   name : "Name",
                   value : businessOwner.businessOwnerName
               });
    owner.push({
                   name : "Email",
                   value : businessOwner.businessOwnerEmail
               });
    owner.push({
                   name : "Website",
                   value : businessOwner.businessOwnerSite
               });
    owner.push({
                   name : "Description",
                   value : businessOwner.businessOwnerDescription
               });
    var details = businessOwner.businessOwnerProperties;
    for(var key in details){
        if (key != null) {
            var ownerProperties = details[key];
            var ownerProperty = JSON.parse(JSON.stringify(ownerProperties));
            owner.push({
                           name : key,
                           value : ownerProperty["propertyValue"]
                       });
        }
    }

    return owner;
}