 Graphics3D 1024,768,32,1

SetBuffer BackBuffer()
SeedRnd MilliSecs()
;;;;;;;;;;;;;;;;camera;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  camera=CreateCamera()
  CameraViewport camera,0,0,1024,768
  CameraRange camera,.01,1000
  PositionEntity camera,0,0,-20

;;;;;;;;;;;;;;;;;;;;;Sounds;;;;;;;;;;;;;;;;;;;;;;;;;;
world=LoadSound("splash.wav")
hammer=LoadSound("hammer.wav")
chop=LoadSound("chop.wav")
button=LoadSound("button.wav")
click=LoadSound("click.wav")
worldchn=PlaySound(world)
;;;;;;;;;;;;;;;;;;;;;;;;;;Load images;;;;;;;;;;;;;;;;;;;;;;;;
 splash=LoadImage("splash.bmp")
map=LoadImage("grass.jpg")
pointer=LoadImage("cursor.bmp")
oakimage=LoadImage("oak.bmp")
pineimage=LoadImage("pine.bmp")
dirt=LoadImage("dirt.bmp")
hud=LoadImage("hud.bmp")
MidHandle dirt
MidHandle pineimage
MidHandle oakimage
;;;;;;;;;;;;;;;;;;;;;;;;animated images;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
house=LoadAnimImage("cabin.bmp",50,50,0,4)
workerimg=LoadAnimImage("worker.bmp",20,20,0,4)
workerchop=LoadAnimImage("workerchop.bmp",20,20,0,4)
workerbutton=LoadAnimImage("workerbutton.bmp",43,50,0,2)
MidHandle house
;;;;;;;;;;;;;;;;;;;;GLOBALS;;;;;;;;;;;;;;;;;;;;;

 Global mapx#,mapy#,imageframe=0,getwood=1,drink=2,eat=3,rest=4,gohome=5,chopping=6

;;;;;;;;;;;;;;;;;;;;;;;;;game speed timer;;;;;;;;;;;;;;;;;;;;;;
timer=CreateTimer(60)



;showsplash
.ssplash
If Not ChannelPlaying(worldchn) And nobgm=0 Then restartmusic=1
While restartmusic=1
Gosub playbgm
Wend  
DrawImage splash,1,1
Flip
If Not MouseHit(1) Then Goto ssplash

;types
Type tree
Field seed
Field x
Field y
Field wood
Field count
Field id
Field harvested
Field breed
End Type 
;type worker
Type worker
Field action
Field move
Field x
Field y
Field hunger
Field thirst
Field fatigue
Field wood
Field stone
Field job
Field targetx
Field targety
Field target
Field timer
End Type 
;type castle
Type home
Field wood
Field stone
Field money
End Type



;spawn world
;spawn trees
For a=1 To 10
newtree.tree= New tree
treecount=treecount+1
creationid=creationid+1
newtree\seed=0
newtree\x=Rand(20,1900)
newtree\y=Rand(20,1060)
newtree\wood=Rand(10,50)
newtree\count=treecount
newtree\id=creationid
newtree\harvested=0
newtree\breed=Rand(1,2)
Next
plantseed=1
Gosub sorttrees



  newhome.home= New home
fileout=WriteFile ("gametest.txt")


;feedback globals
min$="minutes"
sec$="seconds"
trc$="treecount"
pop$="population"
lps$="loops per second"
feedback=MilliSecs()
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;mainloop;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
While Not KeyHit(1)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;FEEDBACK CALCULATION LOOP;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
If KeyHit(17) Then auto=auto+1
If auto=2 Then auto=0
If auto=1 And wood>100 Then Gosub createworker

loop=loop+1
If MilliSecs() > feedback+60000 Then
minutes=minutes+1
feedback=MilliSecs()
loopcheck=loop/60
loop=0
WriteString(fileout,min$)
WriteString(fileout,Str minutes)
WriteString(fileout,trc$)
WriteString(fileout,Str treecount)
WriteString(fileout,pop$)
WriteString(fileout,Str population)
WriteString(fileout,lps$)
WriteString(fileout,Str loopcheck)
If KeyHit(17) Then auto=auto+1
If auto=2 Then auto=0
If auto=1 And wood>100 Then Gosub createworker

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
EndIf 
;loop music
If Not ChannelPlaying(worldchn) Then restartmusic=1
While restartmusic=1
Gosub playbgm
Wend  


;;;flip animated images;;;;
If MilliSecs() > flipanimation+150 Then
flipanimation=MilliSecs()
imageframe=imageframe+1
If imageframe=4 Then imageframe=0
End If

;down to business
Cls

;draw/scroll terrain
If MouseX()<10 Then mapx#=mapx#+5
If MouseX()>GraphicsWidth()-10 Then mapx#=mapx#-5
If MouseY()<10 Then mapy#=mapy#+5
If MouseY()>GraphicsHeight()-10 Then mapy#=mapy#-5
If mapy#>30 Then mapy#=30
If mapy#<-342 Then mapy#=-342
If mapx#>30 Then mapx#=30
If mapx#<-924 Then mapx=-924
TileImage map,mapx#,mapy#
;draw house dirt
If placehouse=2 Then DrawImage dirt,housex+mapx#,housey+mapy#
;draw workers
Color 255,255,255
For work.worker=Each worker
If Not work\job=chopping Then
DrawImage workerimg,work\x+mapx#,work\y+mapy#,imageframe
Else DrawImage workerchop,work\x+mapx#,work\y+mapy#,imageframe

EndIf 
Next 




;placehouse
If placehouse=0 Then
Text 400,400,"Place Your Home"
DrawImage house,MouseX(),MouseY(),imageframe
If MouseHit(1) Then housex=MouseX()-mapx# housey=MouseY()-mapy# placehouse=1 PlaySound(hammer)
workerbrain=MilliSecs()
End If 


;;;;;;workers;;;;;;;;;;;;;;;
If placehouse=1 Then
;initial worker
population=population+1
newworker.worker=New worker
newworker\x=housex newworker\y=housey
newworker\hunger=100
newworker\thirst=100
newworker\fatigue=100
newworker\job=0 
placehouse=2
EndIf 

;;;;;;;;;;;;;;;;;;;;;;workers figure out what to do  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;                                                
If MilliSecs() > AI+1000 Then
AI=MilliSecs()
For work.worker=Each worker
workercheck=work\job
If work\job=gohome And work\x=housex And work\y=housey Then
wood=wood+work\wood
stone=stone+work\stone
work\job=0 
EndIf 
If work\job=0 Then Gosub assignjob
Next
EndIf
 
;move to target
For work.worker=Each worker
If work\targetx > work\x Then work\x=work\x+1
If work\targetx < work\x Then work\x=work\x-1
If work\targety > work\y Then work\y=work\y+1
If work\targety < work\y Then work\y=work\y-1
If work\targety = work\y And  work\targetx = work\x Then 

;start cutting if at tree
If work\job=getwood Then
work\job=chopping 
EndIf 
;chop tree
If work\job=chopping Then 
If MilliSecs()>work\timer+150 Then
work\timer=MilliSecs() work\action=work\action+1 work\move=work\move+1 
If work\action=1 Then PlaySound(chop)
If work\action=20 Then
work\action=0
work\move=0
For ab.tree=Each tree
If work\target=ab\id Then work\wood=ab\wood Delete ab Gosub returnhome
Next
EndIf
EndIf 
EndIf
EndIf 
Next

 


;drawhouse

If placehouse=2 Then DrawImage house,housex+mapx#,housey+mapy#,imageframe

;draw & recycle trees
For plant.tree=Each tree
plant\seed = Rand(1,100*treecount) ;*treecount
If plant\seed =1 Then Gosub createtree
If plant\seed =1 Then Gosub sorttrees
If plant\seed =1 Then Gosub sorttrees
plant\seed=0
If plant\wood=0 Then Delete plant
If plant\breed=1 Then DrawImage pineimage,plant\x+mapx#,plant\y+mapy#
If plant\breed=2 Then DrawImage oakimage,plant\x+mapx#,plant\y+mapy#

Next
;Draw Hud 
DrawImage hud,0,0

;Draw idle buttons
DrawImage workerbutton, 10,30,0

;buttons
;create workerbutton
If ImagesCollide(workerbutton,10,30,0,pointer,MouseX(),MouseY(),0) Then
If buttonover=0 Then PlaySound (button)
buttonover=1 DrawImage workerbutton,10,30,1 Text 100,10, "CREATE WORKER- COST 100 WOOD"
EndIf 
If Not ImagesCollide(workerbutton,10,30,0,pointer,MouseX(),MouseY(),0) Then
buttonover=0
EndIf 
If ImagesCollide(workerbutton,10,10,0,pointer,MouseX(),MouseY(),0) And MouseHit(1) And wood>100 Then
PlaySound(click) Gosub createworker
EndIf  


;draw pointer
DrawImage pointer,MouseX(),MouseY()
Text 100,100, "WOOD "+wood






;alpha tag
Text 512,700,"LT GAME ALPHA V-1.2",True 
VWait 
Flip 
;frametimer
WaitTimer(timer)


  Wend
  End
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;Subroutines;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

.assignjob
If treecount>1 Then
Goto woodcutter
Else Return
EndIf 

;create worker
.createworker
wood=wood-100
population=population+1
newworker.worker=New worker
newworker\x=housex newworker\y=housey
newworker\targetx=housex newworker\targety=housey
newworker\hunger=100
newworker\thirst=100
newworker\fatigue=100
newworker\job=5
Return 

;woodcutter shortest route routine
.woodcutter
work\job=getwood
trgdis=1000000
For ab.tree= Each tree
If ab\harvested=0 Then
If ab\x>housex Then checkx=ab\x-housex
If ab\x<housex Then checkx=housex-ab\x
If ab\y>housey Then checky=ab\y-housey
If ab\y<housey Then checky=housey-ab\y
checkdis=checkx+checky
If checkdis<trgdis Then work\target=ab\id trgdis=checkdis work\targetx=ab\x work\targety=ab\y trgid=ab\id
EndIf 
Next
For ab.tree= Each tree
If trgid=ab\id Then ab\harvested=1
Next
treecount=treecount-1
Return




;return home routine
.returnhome
work\targetx=housex work\targety=housey
work\job=gohome
Return 





;create tree
.createtree
treecount=treecount+1
creationid=creationid+1
newtree.tree= New tree
newtree\seed=0
newtree\x=Rand(-50,50)+plant\x
newtree\y=Rand(-50,50)+plant\y
newtree\wood=Rand(10,50)
newtree\count=treecount
newtree\breed=Rand(1,2)
newtree\id=creationid
If newtree\x>1900 Or newtree\x<20 Then treecount = treecount-1 Delete newtree Goto createtree
If newtree\y>1060 Or newtree\y<20 Then treecount = treecount-1 Delete newtree Goto createtree

Return


;sorttrees
.sorttrees
For a=1 To treecount+1
For sort.tree=Each tree
final.tree = Last tree
If sort\y >= final\y Then Insert sort After Last tree
Next
Next
Return



;play background music
.Playbgm 
worldchn=PlaySound(world)
restartmusic=0
Return