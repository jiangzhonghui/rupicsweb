# Just a log helper
log = (args...) ->
    console.log.apply console, args if console.log?
    
# --------------------------------- EDIT IN PLACE
$.fn.editInPlace = (method, options...) ->
    this.each ->
        methods = 
            # public methods
            init: (options) ->
                valid = (e) =>
                    newValue = @input.val()
                    options.onChange.call(options.context, newValue)
                cancel = (e) =>
                    @el.show()
                    @input.hide()
                @el = $(this).dblclick(methods.edit)
                @input = $("<input type='text' />")
                    .insertBefore(@el)
                    .keyup (e) ->
                        switch(e.keyCode)
                            # Enter key
                            when 13 then $(this).blur()
                            # Escape key
                            when 27 then cancel(e)
                    .blur(valid)
                    .hide()
            edit: ->
                @input
                    .val(@el.text())
                    .show()
                    .focus()
                    .select()
                @el.hide()
            close: (newName) ->
                @el.text(newName).show()
                @input.hide()
        # jQuery approach: http://docs.jquery.com/Plugins/Authoring
        if ( methods[method] )
            return methods[ method ].apply(this, options)
        else if (typeof method == 'object')
            return methods.init.call(this, method)
        else
            $.error("Method " + method + " does not exist.")
    
    
# ---------------------------------------- ROUTER
class AppRouter extends Backbone.Router
    initialize: ->
        @currentApp = new Posts
            el: $("#content")
            #log("init approuter...")
    routes:
        ""                          : "index"
        "/sources/:id/posts/:page"   : "posts"
        "/posts/:pid/images"   		 : "images"
    index: ->
        # show blank
        #log("enter index...")
        $("#content").load "/profile"
    posts: (id, page) ->
        # load posts || display app
        currentApp = @currentApp
        #log("enter posts...app:" + currentApp.source)
        $("#content").load "/sources/" + id + "/posts/" + page
        #, (tpl) ->
        #    currentApp.render(id, page)
    images: (pid) ->
        currentApp = @currentApp
        #log("enter images...app:" + currentApp.source)
        $("#images").load "/posts/" + pid + "/images"
            
# ---------------------------------------- Category Panel
class Categories extends Backbone.View
    initialize: ->
        # HTML is our model
        @el.children("li").each (i,category) ->
            new Category
                el: $(category)

# ---------------------------------------- Category
class Category extends Backbone.View
	events:
        "click      .ajax-link"    : "loadIssues"
    initialize: ->
        @id = @el.attr("data-category")
        @name = $(".ajax-link", @el)
        #log("init source...name:" + @name)
        @page = 1
    loadIssues: (e) ->
    	e.preventDefault()
    	#log("enter ajax block...id:" + @id)
    	#href= e.target.href
    	#log("enter ajax block...href:" + href)
    	$("#content").load "/categories/" + @id + "/issues/" + @page, (tpl) ->
            new Issues
                el: $("#posts")

# ----------------------------------------- ISSUES
class Issues extends Backbone.View
	initialize: ->
        # HTML is our model
        $("#issues").children("tr").each (i,issue) ->
            new Issue
                el: $(issue)
        $("#addissue").click @addissue
        #log("init issues...")
	events:
        "click .nav_page"              : "loadpage"
        #"click .addissue"              : "addissue"
    render: (id) ->
        @id=id
        #log("iid:"+id)
        
        #log("render...issues")
    loadpage: (e) ->
        e.preventDefault()
        href= e.target.href
        #log("href:"+href)
        $("#content").load href, (tpl) ->
            new Issues
                el: $("#posts")
    addissue: (e) ->
        e.preventDefault()
        #log("enter addIssue...")
        jsRoutes.controllers.Issues.addIssue($("#addissue").attr("data-category")).ajax
            success: (data) ->
                _view = new Issue
                    el: $(data).appendTo("#issues")
                _view.el.find(".name").editInPlace("edit")
            error: (err) ->
                # TODO: Deal with                   

# ----------------------------------------- ISSUE
class Issue extends Backbone.View
	initialize: ->
        @id = @el.attr("data-issue")
        @name = $(".name", @el).editInPlace
            context: this
            onChange: @renameIssue
	events:
        "click .name"              : "loadimg"
        "click .delete"              : "deleteIssue"
	loadimg: (e) ->
    	e.preventDefault()
    	href= e.target.href
    	#log("href:"+href)
    	$("#images").load href, (tpl) ->
            new Images
                el: $("#images")
	renameIssue: (name) ->
    	if name.length > 0
            @loading(true)
            jsRoutes.controllers.Issues.renameIssue(@id, name).ajax
                context: this
                success: (data) ->
                    @loading(false)
                    @name.editInPlace("close", data)
                error: (err) ->
                    @loading(false)
                    $.error("Error: " + err)
    	else
            options = $.parseJSON('{"text":"Issue name cannot be null!","layout":"center","type":"alert"}')
            noty(options)
	deleteIssue: (e) ->
    	@loading(true)
    	e.preventDefault()
    	#log("deleteIssue:"+@id)
    	jsRoutes.controllers.Issues.deleteIssue(@id).ajax
            context: this
            success: (tpl) ->
                @loading(false)
                @el.remove()
            error: (err) ->
                @loading(false)
                $.error("Error: " + err)
                log("error:"+err);      
	loading: (display) ->
        if (display)
            @el.children(".delete").hide()
            @el.children(".loader").show()
        else
            @el.children(".delete").show()
            @el.children(".loader").hide()          

# ---------------------------------------- Channel
class Channel extends Backbone.View
    initialize: ->
        # HTML is our model
        @el.children("li").each (i,source) ->
            new Source
                el: $(source)

# ---------------------------------------- Source
class Source extends Backbone.View
	events:
        "click      .ajax-link"    : "loadPosts"
        "click      .allinone"    : "loadImages"
        "click      .favorite"     : "loadFavorites"
        "click      .downloadsource"     : "download"
    initialize: ->
        @id = @el.attr("data-source")
        @name = $(".ajax-link", @el)
        #log("init source...name:" + @name)
        @page = 1
    loadPosts: (e) ->
    	#log("enter ajax block...id:" + @id)
    	$("#content").load "/sources/" + @id + "/posts/" + @page, (tpl) ->
            new Posts
                el: $("#posts")
    	e.preventDefault()
    loadImages: (e) ->
    	#log("enter allinone block...id:" + @id)
    	$("#content").load "/sources/" + @id + "/images/" + @page, (tpl) ->
            new SourceImages
                el: $("#posts")
    	e.preventDefault()
    loadFavorites: (e) ->
    	e.preventDefault()
    	$("#content").load "/favorite/1", (tpl) ->
            new Posts
                el: $("#posts")
    download: (e) ->
        e.preventDefault()
        @loading(true)
        jsRoutes.controllers.Sources.download(@id).ajax
            context: this
            success: (tpl) ->
                @loading(false)
                options = $.parseJSON('{"text":"Successfully download all images!","layout":"center","type":"alert"}')
                noty(options)
                @loadPosts()
            error: (err) ->
                @loading(false)
                options = $.parseJSON('{"text":"Failed to download all images!","layout":"center","type":"alert"}')
                noty(options)
    loading: (display) ->
        if (display)
            @el.children(".download").hide()
            @el.children(".loader").show()
        else
            @el.children(".download").show()
            @el.children(".loader").hide()       
# ----------------------------------------- POSTS
class Posts extends Backbone.View
	initialize: ->
        # HTML is our model
        @el.children("tr").each (i,post) ->
            new Post
                el: $(post)
        #log("init posts...")
	events:
        "click .nav_page"              : "loadpage"
    render: (id) ->
        @id=id
        #log("sid:"+id)
        
        #log("render...posts")
    loadpage: (e) ->
    	e.preventDefault()
    	href= e.target.href
    	#log("href:"+href)
    	$("#content").load href, (tpl) ->
            new Posts
                el: $("#posts")

# ----------------------------------------- POST
class Post extends Backbone.View
	initialize: ->
        @id = @el.attr("data-post")
        @name = $(".ajax-link", @el)
        #log("init post...name:" + @name)
	events:
        "click .ajax-link"              : "loadimg"
        "click .favorite"              : "favorite"
    render: (id, page) ->
        @source = id
        # HTML is our model
        @page = page
    loadimg: (e) ->
    	e.preventDefault()
    	href= e.target.href
    	#log("href:"+href)
    	$("#images").load href, (tpl) ->
            new Images
                el: $("#images")[0]
    favorite: (e) ->
    	e.preventDefault()
    	#log("favorite:"+@id)
    	jsRoutes.controllers.Posts.favorite(@id).ajax
            context: this
            success: (tpl) ->
                _span = $("span",@el)[0]
                _class = _span.getAttribute "class"
                if(_class == "label")
                    _span.setAttribute "class", "label label-success"
                else
                    _span.setAttribute "class", "label"
            error: (err) ->
                $.error("Error: " + err)
                #log("error:"+err)
                
# ----------------------------------------- IMAGES
class Images extends Backbone.View
	initialize: ->
        $("#selectAllImages").change @selectAll
        $("#deleteImages").click @delete
        handler = null
        options =
            autoResize: true
            container: $("#images-container")
            offset: 20
            itemWidth: 210
        handler = $("#sortable li")
        handler.wookmark options
        $(".fancybox").fancybox
            nextClick: true
            openEffect: "elastic"
            prevEffect: "elastic"
            nextEffect: "elastic"
            helpers:
                title:
                    type: "inside"
                thumbs:
                    width: 100
                    height: 100
            afterLoad: ->
                @title = "Image " + (@index + 1) + " of " + @group.length + (if @title then " - " + @title else "")
        if($("#fileupload").length > 0)
            $("#fileupload").fileupload
                url: @el.attr("data-url")
                dataType: "json"
                done: (e, data) ->
                    $.each data.result.files, (index, file) ->
                        $("<p/>").text(file.name).appendTo "#files"

                progressall: (e, data) ->
                    progress = parseInt(data.loaded / data.total * 100, 10)
                    $("#progress .bar").css "width", progress + "%"
                $('#fileupload').prop
                    'disabled'
                    !$.support.fileInput
                $('#fileupload').parent
                $('#fileupload').addClass
                    $.support.fileInput ? undefined : 'disabled'
            $( "#sortable" ).sortable
                revert: true
            $( "ul, li" ).disableSelection();
            $( "#sortImages" ).click @sortImages
        if($("#importImages").length > 0)
            $("#importImages").click @importImg
            jsRoutes.controllers.Issues.showIssueSelector().ajax
                context: this
                success: (tpl) ->
                    _issueSelector = $("#issue-selector",@el)
                    _issueSelector.after(tpl)
                error: (err) ->
                    $.error("Error: " + err)
                    #log("error:"+err);
	#events:
        #"click .ajax-link"              : "loadimg"
        #"click .favorite"              : "favorite"
    selectAll: (e) ->
    	$(".importSelector",@el).each () ->
            #this.checked = e.target.checked
            if(e.target.checked)
                $(this).attr('checked','checked')
            else
                $(this).removeAttr('checked')
    delete: (e) ->
    	e.preventDefault()
    	allVals = []
    	$(".importSelector",@el).each () ->
            if(this.checked == true) 
                allVals.push(this.value)
    	jsRoutes.controllers.Images.deleteImages(allVals).ajax
            context: this
            success: (tpl) ->
                options = $.parseJSON('{"text":"Successfully delete images!","layout":"center","type":"alert"}')
                noty(options)
            error: (err) ->
                options = $.parseJSON('{"text":"Failed to delete images!","layout":"center","type":"alert"}')
                noty(options)                              
    importImg: (e) ->
    	e.preventDefault()
    	allVals = []
    	issueid = parseInt($("#issueSelector").val())
    	$(".importSelector",@el).each () ->
            if(this.checked == true) 
                allVals.push(this.value)
    	jsRoutes.controllers.Images.importImages(allVals, issueid).ajax
            context: this
            success: (tpl) ->
                options = $.parseJSON('{"text":"Successfully import images!","layout":"center","type":"alert"}')
                noty(options)
            error: (err) ->
                options = $.parseJSON('{"text":"Failed to import images!","layout":"center","type":"alert"}')
                noty(options)    
    sortImages: (e) ->
    	e.preventDefault()     
    	issueid = e.target.dataset.issue  
    	allVals = $("#sortable").sortable('toArray')
    	jsRoutes.controllers.Images.sortImages(allVals, issueid).ajax
            context: this
            success: (tpl) ->
                options = $.parseJSON('{"text":"Successfully sort images!","layout":"center","type":"alert"}')
                noty(options)
            error: (err) ->
                options = $.parseJSON('{"text":"Failed to sort images!","layout":"center","type":"alert"}')
                noty(options)
                
# ----------------------------------------- SOURCE IMAGES
class SourceImages extends Backbone.View
	initialize: ->
        $("#selectAllImages").change @selectAll
        handler = null
        curPage = 1
        isScrolling = false
        onScroll = (event) ->
            winHeight = (if window.innerHeight then window.innerHeight else $(window).height())
            closeToBottom = ($(window).scrollTop() + winHeight > $(document).height() - 100)
            if closeToBottom and !isScrolling
                isScrolling = true
                curPage=curPage+1
                id = $("#sortable")[0].dataset.source
                jsRoutes.controllers.Sources.images(id, curPage).ajax
                    context: this
                    success: (tpl) ->
                        $("#sortable").append tpl
                        handler.wookmarkInstance.clear()  if handler.wookmarkInstance
                        handler = $("#sortable li")
                        handler.wookmark options
                        isScrolling = false
                    error: (err) ->
                        $.error("Error: " + err)
                        #log("error:"+err);
                        isScrolling = false
        options =
            autoResize: true
            container: $("#images-container")
            offset: 20
            itemWidth: 210
        $(window).bind "scroll", onScroll
        handler = $("#sortable li")
        handler.wookmark options
        $(".fancybox").fancybox
            nextClick: true
            openEffect: "elastic"
            prevEffect: "elastic"
            nextEffect: "elastic"
            helpers:
                title:
                    type: "inside"
                thumbs:
                    width: 100
                    height: 100
        
        if($("#importImages").length > 0)
            $("#importImages").click @importImg
            jsRoutes.controllers.Issues.showIssueSelector().ajax
                context: this
                success: (tpl) ->
                    _issueSelector = $("#issue-selector",@el)
                    _issueSelector.after(tpl)
                error: (err) ->
                    $.error("Error: " + err)
                    #log("error:"+err);
    selectAll: (e) ->
    	$(".importSelector",@el).each () ->
            #this.checked = e.target.checked
            if(e.target.checked)
                $(this).attr('checked','checked')
            else
                $(this).removeAttr('checked')
    importImg: (e) ->
    	e.preventDefault()
    	allVals = []
    	issueid = parseInt($("#issueSelector").val())
    	$(".importSelector",@el).each () ->
            if(this.checked == true) 
                allVals.push(this.value)
    	jsRoutes.controllers.Images.importImages(allVals, issueid).ajax
            context: this
            success: (tpl) ->
                options = $.parseJSON('{"text":"Successfully import images!","layout":"center","type":"alert"}')
                noty(options)
            error: (err) ->
                options = $.parseJSON('{"text":"Failed to import images!","layout":"center","type":"alert"}')
                noty(options)    
            
# ------------------------------------- INIT APP
$ -> # document is ready!

    app = new AppRouter()
    channel = new Channel el: $("#sources")
    categories = new Categories el: $("#categories")

    Backbone.history.start
        pushHistory: true