var ischool = ischool || {};	//namespace : ischool

ischool.WindowResizeHandler = {};  //播放筆劃的物件，只負責播放筆劃

/*
	在拖拉更改視窗大小的過程會一直觸發 window.resize 事件，造成效能慢，
	這段程式碼偵測使用者改變視窗大小的動作已結束之後，才觸發事件。
*/
ischool.WindowResizeHandler.handle = function(afterWindowResize) {


/* 檢查 resize 是否結束了 */
	var rtime = new Date(1, 1, 2000, 12,00,00);
	var timeout = false;
	var delta = 200;

	/*
		idea : 當window 經過 200 millisecond 都沒有觸發 resize 事件，
		就認定其已經完成調整視窗大小的動作。
	*/
	$(window).resize(function() {
	    rtime = new Date();
	    if (timeout === false) {
	        timeout = true;
	        setTimeout(resizeend, delta);
	    }
	});

	var resizeend = function() {
	    if (new Date() - rtime < delta) {
	        setTimeout(resizeend, delta);
	    } else {
	        timeout = false;
	        if (afterWindowResize)
	        	afterWindowResize();	
	    }               
	};
}

var ischool = ischool || {};	//namespace : ischool

ischool.CanvasResizer = {};  //播放筆劃的物件，只負責播放筆劃

ischool.CanvasResizer.handle = function(options) {

	//containerID, widthDiff, heightDiff, afterResizedHandler

	var targetContainerID = options.containerID ;
	var targetCanvasID ='myCanvas';

	var widthDiff = (options.widthDiff) ? options.widthDiff : 0;
	var heightDiff = (options.heightDiff) ? options.heightDiff : 0;
	var afterResizedHandler = options.afterResizedHandler;

	var canvasWidth = 0;
	var canvasHeight = 0;

	/* 處理 Window Resize 事件，確定 Window Resize 後才會觸發事件，而不會在 Window Resize 過程中一直觸發，影響效能 */
	ischool.WindowResizeHandler.handle(function() {
		resizeUI();
	});

	var resizeUI = function(){

		canvasWidth = $('#' + targetContainerID ).width() - 3;
		canvasHeight = $('#' + targetContainerID ).height() - 3;

		/* 若還沒有 Canvas */
		resetCanvas(canvasWidth, canvasHeight, targetCanvasID);	

		if (afterResizedHandler) {
			afterResizedHandler();
		}
	};

	var resetCanvas = function(canvasWidth, canvasHeight, canvasID) {
		$('#' + targetContainerID).html('');
		var canvasString = "<canvas id='" + canvasID + "' width='" + canvasWidth + "' height='" + canvasHeight + "' style='padding: 0px; margin: 0px; border: 0px; overflow: hidden;'></canvas>";
		$(canvasString).appendTo('#' + targetContainerID);
	};

	resizeUI();

}



	//定義 Touch drawing 行為的 jquery plugin
	//var defineTouchDrawing = function() {
		$.fn.drawTouch = function(option) {

			var defaults = {
				onDrawing : null,
				onStart : null,
				onEnd : null,
				lineWidth : 4,
				lineColor : "#000000"
			};

			var _lineColor = (option.lineColor) ? (option.lineColor) : "#000000";


			var options = $.extend(defaults, option) ;

			return this.each(function(){

				var raiseDrawingCallback = function(x, y, color) {
					if (options.onDrawing) {
						options.onDrawing({time: (new Date()).getTime(), x: x, y: y }); //, lw: options.lineWidth, c: color});
					}
				};

				var el = this;
			  	var ctx = el.getContext("2d");
			  	ctx.lineWidth = options.lineWidth;
			  	var canvasX = $(el).offset().left;
			    var canvasY = $(el).offset().top;

			    var start = function(e) {
			        var touchEvent = e.originalEvent.changedTouches[0];
			        ctx.beginPath();  
			        ctx.lineJoin = 'round';
			        ctx.lineCap = "round";
			        //var x = touchEvent.pageX;
			        //var y = touchEvent.pageY;
			        var x = Math.floor(touchEvent.pageX - canvasX );
			        var y = Math.floor(touchEvent.pageY - canvasY );
			        ctx.moveTo(x, y);
			        
			        if (options.onStart){
			        	options.onStart();
			        }

			        raiseDrawingCallback(x, y, _lineColor);
			    };
			    var move = function(e) {
			        var touchEvent = e.originalEvent.changedTouches[0];
			        e.preventDefault();
			        var x = Math.floor(touchEvent.pageX - canvasX );
			        var y = Math.floor(touchEvent.pageY - canvasY );

			        ctx.lineTo(x, y);
			        ctx.strokeStyle = options.lineColor ;
			        ctx.stroke();
			        raiseDrawingCallback(x, y, _lineColor);
			        //console.log(options);
			        //alert("touchmove");
			    };

			    var end = function(e) {
			    	//alert("touchend");
			    	if (options.onEnd)
			    		options.onEnd();
			    }
			    $(this).on("touchstart", start);
			    $(this).on("touchmove", move);
			    $(this).on("touchend", end);
			    $(this).on("touchleave", end);
			});
		};
	//};


	//定義 mouse drawing 行為的 jquery plugin
	//var defineMouseDrawing = function() {
		$.fn.drawMouse = function(option) {

			var defaults = {
				onDrawing : null,
				onStart : null,
				onEnd : null,
				lineWidth : 4,
				lineColor : "#000000"
			};

			var options = $.extend(defaults, option) ;

			var _lineColor = (option.lineColor) ? (option.lineColor) : "#000000";

			return this.each(function(){

				var raiseDrawingCallback = function(x, y, color) {
					if (options.onDrawing){
						options.onDrawing({time: (new Date()).getTime(), x: x, y: y }); //, lw: options.lineWidth, c:color});
					}
				};

				var el = this;
			  	var ctx = el.getContext("2d");
			  	ctx.lineWidth = options.lineWidth ;
			  	var canvasX = $(el).offset().left;
			    var canvasY = $(el).offset().top;
			    var clicked = 0;

			    var previousX ;
			    var previousY;

			    var start = function(e) {
			        clicked = 1;
			        ctx.beginPath();
			        ctx.lineJoin = 'round';
			        ctx.lineCap = "round";
			        //var x = e.pageX - el.offsetX;
			        //var y = e.pageY - el.offsetY ;
			        var x = Math.floor(e.pageX - canvasX );
			        var y = Math.floor(e.pageY - canvasY );
			        previousX = x;
			        previousY = y;
			        ctx.moveTo(x, y);

			        if (options.onStart)
			        	options.onStart();

			        raiseDrawingCallback(x, y, options.lineColor);
			    };
			    
			    var move = function(e) {
			        if(clicked){
			        	//var x = e.pageX;
			        	//var y = e.pageY;
			        	var x = Math.floor(e.pageX - canvasX );
			        	var y = Math.floor(e.pageY - canvasY );

			            ctx.lineTo(x, y);
			            //ctx.arcTo(previousX, previousY, x, y , 5);
			            ctx.strokeStyle = options.lineColor ;
			            //console.log("color:" + _lineColor); //為什麼設定幾次就會有幾個值？
			            ctx.stroke();
			            previousX = x;
			        	previousY = y;
			            //console.log(x + "," + y);
			            raiseDrawingCallback(x, y, _lineColor);
			        }
			    };

			    var end = function(e) {
			        clicked = 0;

			        if (options.onEnd)
			        	options.onEnd();
			    };

			    $(this).on("mousedown", start);
			    $(this).on("mousemove", move);
			    $(this).on("mouseup", end);
			    $(this).on("mouseout", end);

			});
		};
	//};



var ischool = ischool || {};	//namespace : ischool

ischool.StrokePlayer = {};  //播放筆劃的物件，只負責播放筆劃

/* 宣告一個『建立播放筆劃物件』的方法 */
ischool.StrokePlayer.create = function(containerID) {

	var aryStrokes = [];	//記錄所有筆劃
	var canvasID = "myCanvas";	//預設建立的 canvas 的 ID
	var playerQueue = [];	//放置 Painter 的集合。一筆劃(stroke)由一個 Painter 物件處理。
	var playInterval =  100;	//millisecond, 每隔多少時間取一次筆劃的點資料來畫圖。
	var speed =  1 ;	//播放速度，浮點數。
	var afterCanvasResize ;	//將 Canvas Resize 事件廣播出去。因為 window resize 時會把 Canvas 重設，所以把。

	ischool.CanvasResizer.handle({
		containerID : containerID,
		widthDiff : 0,
		heightDiff : 0,
		afterResizedHandler : function() {
			//重劃所有的筆劃
			if (drawAllPoints)
				drawAllPoints();

			//將 Canvas Resize 事件廣播出去。
			if (afterCanvasResize)
				afterCanvasResize();
		}
	});

	/* 此函數要確保這些 painter 物件可以按照順序執行，
		一個 stroke 執行完畢後才可以執行下一個 stroke 。
	*/
	var playByOrder = function() {
		var isProcessing = false 
		var i=0;
		for(i=0; i<playerQueue.length; i++) {
			item = playerQueue[i] ;
			if (item.getStatus() == 1){	//處理中 
				isProcessing = true ;
				break ;
			}
		}

		if (!isProcessing) {
			for(i=0; i<playerQueue.length; i++) {
				item = playerQueue[i] ;
				if (item.getStatus() == 0){	//尚未處理
					item.draw( speed, playInterval );
					break ;
				}
			}
		}
	};

	var drawAllPoints = function() {
		//再重劃所有點
		$(aryStrokes).each(function(index, stroke) {
			if (stroke) {
				if (stroke.points.length > 0) {
					var ctx = document.getElementById(canvasID).getContext('2d');
					ctx.beginPath();  //若不加此行則全部會變成最後的顏色及粗細
					ctx.lineWidth = stroke.pen;
					ctx.strokeStyle = stroke.color ;
					ctx.lineJoin = 'round';
			        ctx.lineCap = "round";

					var initPt = stroke.points[0]
					ctx.moveTo(initPt.x, initPt.y);

					$(stroke.points).each(function(index, pt) {
						ctx.lineTo(pt.x, pt.y);
			            //console.log("color:" + _lineColor); //為什麼設定幾次就會有幾個值？
			            ctx.stroke();	
					});
	            }
        	}
		});
	}


	return {

		/* 
			播放所有筆劃，按照順序與時間。
			strokes : 所有筆劃資料
			drawSpeed : 播放速度， float number
			drawInterval : 取得點資料的時間間隔，值越小畫面越順暢(不會 lag)。 millisecond。
		*/
		play : function(strokes, drawSpeed, drawInterval) {
			this.clean();	//清空畫面
			this.stop();	//停止目前所有播放動作
			if (strokes) {
				aryStrokes = strokes;
				playerQueue = [];
				$(aryStrokes).each(function(index, stroke) {
					
					/* 傳入要畫的點集合，以及畫完後要呼叫的函數 */
					var painter = StrokePainter(canvasID, stroke, playByOrder) ;
					if (painter) {
						playerQueue.push(painter);
					}
				});

				if (drawInterval)
					playInterval = drawInterval ;	//重設取點資料畫出的時間間隔

				if (drawSpeed)
					speed = drawSpeed ;

				playByOrder();
			}
		},

		/* 清除畫面成為空白 */
		clean : function() {
			//alert(data);
			var el = document.getElementById(canvasID);
	  		var ctx = el.getContext("2d");
	    	ctx.clearRect(0,0,el.width,el.height);
		},

		/* 
			全部立刻重劃，不需要按時間重劃。
			若傳入筆劃（strokes)，就畫出傳入的筆劃。否則畫出內部儲存的筆劃 (aryStrokes)。
		*/
		drawNow : function(strokes) {
			this.clean();
			aryStrokes = strokes ;
			drawAllPoints();
		},

		/* 立刻停止 */
		stop : function() {
			for(i=0; i<playerQueue.length; i++) {
				item = playerQueue[i] ;
				if (item.getStatus() != 2){	//尚未完成
					item.stop();
				}
			}
		},

		/* 取得有多少筆劃 */
		getStrokeCount : function() {
			return aryStrokes.length ;
		},

		/* 播放單一筆劃。 */
		addStroke : function(stroke) {
			aryStrokes.push(stroke);
			if (stroke.points.length ==0) 
				return ;

			/* 傳入要畫的點集合，以及畫完後要呼叫的函數 */
			var painter = StrokePainter(canvasID,stroke, playByOrder) ;
			if (painter) {
				playerQueue.push(painter);
			}

			playByOrder();
		},

		/* 取得系統建立的 CanvasID  */
		getCanvasID : function() {
			return canvasID;
		},

		setCanvasResizeHandler : function(afterCanvasResizedHandler) {
			afterCanvasResize = afterCanvasResizedHandler
		},

		setBackgroundImage : function(bgOptions) {
			var $container = $('#' + containerID) ;
			if (bgOptions.color)
				$container.css("background-color", bgOptions.color);
			if (bgOptions.imageUrl)
				$container.css("background-image", "url(" + bgOptions.imageUrl + ")" );
			if (bgOptions.repeat)
				$container.css("background-repeat", bgOptions.repeat);
			if (bgOptions.position)
				$container.css("background-position", bgOptions.position);
			if (bgOptions.size)
				$container.css("background-size", bgOptions.size);
		},

		getImageDataUrl : function() {
			var elCanvas = document.getElementById(canvasID);
			if (elCanvas)
				return elCanvas.toDataURL("image/png");
			else
				return undefined;
		}
	};
}

/*  factory function to create a player object for replay points */
var StrokePainter = function(canvasID, stroke, finishHandler) {

	if (! stroke)
		return null;

	if (stroke.points.length ==0)
		return null ;

	var status = 0 ;	//0: 尚未開始 , 1: 處理中 , 2: 已處理完成
	var ctx ;

	var pts = stroke.points ;
	var currentIndex = 0;
	var lineColor = stroke.color;
	var lineWidth = stroke.pen;

	var initPt = pts[0];	
	var initTime = initPt.time ;	//第一個點的時間
	var beginPlayTime ;	//開始play 的時間

	var speed = 1;	//播放速度
	var playInterval =100;	//取樣時間

	var isInterrupted = false ;	//是否中斷。當執行 stop 時，會將此變數設為 true。

	var beginDraw = function() {
		status = 1;
		/* 這筆劃開始畫點時的時間 */
		if (!beginPlayTime)
			beginPlayTime = (new Date()).getTime();

		/*  ctx : 因為這段程式碼速度慢，如果放在上面，到執行 beginDraw 會太慢，
		    導致兩個 Painter 同時執行，會造成贅連線的狀況，所以放在這裡判斷，讓前面的程式執行快一點，好更改 status 的狀態。 */
		if (!ctx) {
			var el = document.getElementById(canvasID)
			ctx = el.getContext("2d");
			ctx.beginPath(); 
			ctx.lineWidth = lineWidth;
			ctx.strokeStyle = lineColor ;
			ctx.lineJoin = 'round';
			ctx.lineCap = "round";
			ctx.moveTo(initPt.x, initPt.y);
			//console.log( " move to (" + initPt.x + "," + initPt.y + ")");
		}

		//console.log(' == beginDraw ==  currentIndex : ' + currentIndex );
		if (pts.length ==0)
			return ;

		if (currentIndex > pts.length -1)
			return ;

		var firstPt = pts[currentIndex];

		var i = currentIndex;
		var currentPlayTime = (new Date()).getTime();	//這一輪 play 的時間，因為每 playInterval 長度就 play 一輪。
		var currentTimeDiff = (currentPlayTime - beginPlayTime) * speed ;	//這一輪距離開始 play已經過了多少時間？

		for (i=currentIndex; i<pts.length; i++) {
			//判斷是否中斷
			if (isInterrupted) {
				status = 2;
				//console.log(' == isInterrupted ==  currentIndex : ' + currentIndex );
				if (finishHandler)
					finishHandler();
				return ;
			}

			var pt = pts[i];
			var timeDiff = pt.time - initTime;	//計算這個點的時間距離第一個點過了多少時間
			
			if (timeDiff < currentTimeDiff) {  
				//ctx.lineWidth = pt.lw ;
				ctx.lineTo(pt.x, pt.y);
				ctx.stroke();
				//console.log(i + " : (" + pt.x + "," + pt.y + ") , count :" + pts.length + "," + timeDiff + "," + currentTimeDiff);
			}
			else
				break;
		}
		if (i >= pts.length) {
			status = 2 ;
			if (finishHandler)
				finishHandler();
		}
		else {
			currentIndex = i;
			window.setTimeout(beginDraw, playInterval);	//每隔多少時間取一次筆劃的點資料畫圖
		}
	}

	return {
		draw : function(drawSpeed, drawInterval) {
			speed =drawSpeed ;
			playInterval = drawInterval ;
			isInterrupted = false ;
			beginDraw();
		},
		//0: 尚未開始 , 1: 處理中 , 2: 已處理完成
		getStatus : function() {
			return status;
		},

		stop : function() {
			isInterrupted = true ;
		}
	}
};


var ischool = ischool || {};	//namespace : ischool

ischool.Painter = {};  //類似小畫家的物件，負責讓使用者書寫畫面，並收集筆劃資料，可 play, undo, redo, clean 等。

/* 宣告一個『建立小畫家物件』的方法 */
ischool.Painter.create = function(options) {

	var containerID = options.containerID;
	var penSize = options.penSize;
	var penColor = options.penColor ;
	var afterStrokeHandler = options.afterStroke;	//每一筆劃完成後就會觸發此事件

	//會建立 Canvas，並套用 resize，以及 play 的功能
	var strokePlayer = ischool.StrokePlayer.create(containerID);

	//處理 window.resize 事件
	strokePlayer.setCanvasResizeHandler(function() {
		initCanvasBehavior();
		strokePlayer.drawNow(strokes);
	});

	var canvasID = strokePlayer.getCanvasID();

	var strokes = []; //暫存所有的筆劃
	var undoStrokes = [];	//所有後悔的步驟
	var points =[];	//暫存一筆劃的所有點資訊(x,y,time)

	//套用 Mouse 畫圖行為 (適用 PC)
	var registerMouseDrawing = function() {
		$('#' + canvasID).drawMouse({
			
			lineWidth : penSize ,

			lineColor : penColor,

			onStart : function(evt) {
				points=[];
			},

			onDrawing : function(evt) {
				points.push(evt);
			},

			onEnd : function() {
				var stroke = { color: penColor , pen: penSize, points:points};
				strokes.push(stroke);
				undoStrokes =[];

				if (afterStrokeHandler)
					afterStrokeHandler(stroke);
			}
		});
	};

	//套用 Touch 畫圖行為(適用 行動裝置)
	var registerTouchDrawing = function() {
		$('#' + canvasID).drawTouch({
			
			lineWidth : penSize ,

			lineColor : penColor,

			onStart : function(evt) {
				points=[];	//reset 
			},

			onDrawing : function(evt) {
				points.push(evt);
			},

			onEnd : function() {
				var stroke = { color: penColor , pen: penSize, points:points};
				strokes.push(stroke);
				undoStrokes =[];

				if (afterStrokeHandler)
					afterStrokeHandler(stroke);
			}
		});
	};

	var initCanvasBehavior = function() {
		
		//1. 套用滑鼠繪圖的邏輯
		registerMouseDrawing();
		
		//2. 套用手指繪圖的邏輯 (適用平板)
		registerTouchDrawing();

	};

	initCanvasBehavior();

	/* 改變畫布的顏色和畫筆粗細 */
	var changeColorPen = function() {
		$('#' + canvasID).drawMouse({ lineWidth : penSize, lineColor : penColor });
		$('#' + canvasID).drawTouch({ lineWidth : penSize, lineColor : penColor });
	};


	return {

		replay : function(speed) {
			var theSpeed = (speed) ? speed : 1 ;
			strokePlayer.play( strokes, speed, 10);
		},

		undo : function() {
			strokePlayer.clean();	//清除畫面
	    	var s = strokes.pop();
	    	if (s)
	    		undoStrokes.push(s);
	    	strokePlayer.drawNow(strokes);
		},

		redo : function() {
			strokePlayer.clean();	//清除畫面
	    	var s = undoStrokes.pop();
	    	if (s)
	    		strokes.push(s);
	    	strokePlayer.drawNow(strokes);
		},

		clean : function() {
			strokePlayer.clean();	//清除畫面
			strokes =[];	//清除所有筆畫資料
	        undoStrokes = [];
		},

		getStrokes : function() {
			return strokes;
		},

		setStrokes : function(theStrokes) {
			strokes = theStrokes ;
		},

		setPenColor : function(color) {
			penColor = color ;
			changeColorPen();
		},

		setPenSize : function(size) {
			penSize = size ;
			changeColorPen();
		},

		getStrokePlayer : function() {
			return strokePlayer ;
		},

		setBackgroundImage : function( bgOptions ) {
			strokePlayer.setBackgroundImage(bgOptions);
		},

		getImageDataUrl : function() {
			return strokePlayer.getImageDataUrl();
		}
	}
}
