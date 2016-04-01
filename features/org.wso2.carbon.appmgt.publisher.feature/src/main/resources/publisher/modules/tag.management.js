/*
Description: The class is used to manage and process tags
Filename: tag.management.js
Created Date: 4/10/2013
 */

var tagModule=function(){

    var log=new Log('tag.manager');

    function TagManager(){
       this.tagCloud={};
    }

    /*
    The method is used to process a list of tags
    @tags: A tag array e.g ["/_system/governance/repository/components/org.wso2.carbon.governance;tag1:1",
     "/_system/governance/repository/components/org.wso2.carbon.governance;mytag:2"]

     @type: Type of the asset to which tags are associated
     */
    TagManager.prototype.process = function (tags, type) {
        var tags = tags || [];
        var tag;

        for (var index in tags) {
            tag = tags[index];
            //Break by url
            var components = tag.split(';');
            var tagComponent = components[1] || '';
            var tagDetail = tagComponent.split(':');
            var tagName = tagDetail[0];
            var tagCount = tagDetail[1];

            //Check if the tag cloud has the type
            if (!this.tagCloud.hasOwnProperty(type)) {
                this.tagCloud[type] = {tags_val: {}, totalTagCount: 0};
            }

            this.tagCloud[type].tags_val[tagName] = tagCount;
            this.tagCloud[type].totalTagCount++;

        }
    };

    /*
    The function returns all tags matching a given query after been formatted by
    an optional formatter
    @type: The types of tags that must be removed
    @formatter: A formatting function which will change the structure of the output
    @return: An array of tags containing an id and the name
     */
    TagManager.prototype.get=function(type,predicate,formatter){

        var tagType=this.tagCloud[type]||{};
        var tags=tagType.tags_val||{};
        var formatter=formatter||defaultFormatter;
        var predicate=predicate||defaultPredicate;
        var output=[];
        var counter=0;
        var context={};

        for(var index in tags){
            context={};
            context['index']=counter;
            context['tagName']=index;
            if(predicate(context)){

                output.push(formatter(context));
                counter++;
            }

        }

        return output;

    };

    /*
    The function checks whether the tag manager needs to be updated
    @tags: The tags that must be added
    @return: True if the tags have been updated,else false
     */
    TagManager.prototype.update=function(tags){

        //Update the tag manager if the tag entry count is different
        if(tags.length!=this.tagCloud.totalTagCount){
             this.process(tags);
            return true;
        }

        return false;
    };

    /*
    The
     */
    function defaultFormatter(context){
        return { id:context.index ,name:context.tagName};
    }

    function defaultPredicate(context){
        return true;
    }

    return{
        TagManager:TagManager
    }
};
