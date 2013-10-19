package com.athaydes.automaton

import groovy.util.logging.Slf4j
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.layout.VBox
import javafx.stage.Stage

import java.awt.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

/**
 *
 * User: Renato
 */
class FXAutomaton extends Automaton<FXAutomaton> {

	private static instance

	static synchronized FXAutomaton getUser( ) {
		if ( !instance ) instance = new FXAutomaton()
		instance
	}

	protected FXAutomaton( ) {}

	FXAutomaton clickOn( Node node, Speed speed = DEFAULT ) {
		moveTo( node, speed ).click()
	}

	FXAutomaton doubleClickOn( Node node, Speed speed = DEFAULT ) {
		moveTo( node, speed ).doubleClick()
	}

	FXAutomaton moveTo( Node node, Speed speed = DEFAULT ) {
		def currPos = MouseInfo.pointerInfo.location
		def target = centerOf node
		move( currPos, target, speed )
	}

	FXDragOn<FXAutomaton> drag( Node node ) {
		def target = centerOf node
		new FXDragOn( this, target.x, target.y )
	}

	static Point centerOf( Node node ) {
		def windowPos = new Point( node.scene.window.x.intValue(), node.scene.window.y.intValue() )
		def scenePos = new Point( node.scene.x.intValue(), node.scene.y.intValue() )
		def boundsInScene = node.localToScene node.boundsInLocal
		def absX = windowPos.x + scenePos.x + boundsInScene.minX
		def absY = windowPos.y + scenePos.y + boundsInScene.minY
		[ ( absX + boundsInScene.width / 2 ).intValue(),
				( absY + boundsInScene.height / 2 ).intValue() ] as Point
	}

}

@Slf4j
class FXApp extends Application {

	private static Stage stage
	private static stageFuture = new ArrayBlockingQueue<Stage>( 1 )

	static Scene getScene( ) { initialize().scene }

	synchronized static Stage initialize( ) {
		if ( !stage ) {
			log.debug 'Initializing FXApp'
			Thread.start { launch FXApp }
			stage = stageFuture.poll 10, TimeUnit.SECONDS
			assert stage
			stageFuture = null
		}
		doInFXThreadBlocking { ensureShowing( stage ) }
		log.debug "Stage now showing!"
		stage
	}

	private static void ensureShowing( Stage stage ) {
		stage.show()
		stage.toFront()
	}

	static doInFXThreadBlocking( Closure toRun ) {
		if ( Platform.isFxApplicationThread() )
			toRun()
		else {
			def blockUntilDone = new ArrayBlockingQueue( 1 )
			Platform.runLater { toRun(); blockUntilDone << true }
			assert blockUntilDone.poll( 5, TimeUnit.SECONDS )
		}
	}

	static void startApp( Application app ) {
		initialize()
		Platform.runLater { app.start stage }
	}

	@Override
	void start( Stage primaryStage ) throws Exception {
		primaryStage.scene = new Scene( new VBox(), 600, 500 )
		primaryStage.title = 'FXAutomaton Stage'
		ensureShowing( primaryStage )
		stageFuture.add primaryStage
	}

}

class FXer extends Automaton<FXer> {

	Node node
	def delegate = FXAutomaton.user

	static FXer getUserWith( Node node ) {
		new FXer( node: node )
	}

	FXer clickOn( Node node, Speed speed = DEFAULT ) {
		delegate.clickOn( node, speed )
		this
	}

	FXer clickOn( String selector, Speed speed = DEFAULT ) {
		delegate.clickOn( node.lookup( selector ), speed )
		this
	}

	FXer doubleClickOn( Node node, Speed speed = DEFAULT ) {
		moveTo( node, speed ).doubleClick()
	}

	FXer doubleClickOn( String selector, Speed speed = DEFAULT ) {
		moveTo( node.lookup( selector ), speed ).doubleClick()
	}

	FXer moveTo( Node node, Speed speed = DEFAULT ) {
		delegate.moveTo( node, speed )
		this
	}

	FXer moveTo( String selector, Speed speed = DEFAULT ) {
		delegate.moveTo( node.lookup( selector ), speed )
		this
	}

	FXDragOn<FXer> drag( Node node ) {
		def target = centerOf node
		new FXerDragOn( this, target.x, target.y )
	}

	FXDragOn<FXer> drag( String selector ) {
		def target = centerOf node.lookup( selector )
		new FXerDragOn( this, target.x, target.y )
	}

	Point centerOf( Node node ) {
		delegate.centerOf( node )
	}

}

class FXDragOn<T extends Automaton<? extends Automaton>> extends DragOn<T> {

	protected FXDragOn( T automaton, fromX, fromY ) {
		super( automaton, fromX, fromY )
	}

	T onto( Node node, Speed speed = Automaton.DEFAULT ) {
		def center = FXAutomaton.centerOf( node )
		onto( center.x, center.y, speed )
	}

}

class FXerDragOn extends FXDragOn<FXer> {

	protected FXerDragOn( FXer fxer, fromX, fromY ) {
		super( fxer, fromX, fromY )
	}

	FXer onto( String selector, Speed speed = Automaton.DEFAULT ) {
		def node = automaton.node.lookup( selector )
		onto( node, speed )
	}
}