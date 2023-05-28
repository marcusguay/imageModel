const tf = require('@tensorflow/tfjs-node');
const fs = require('fs');
const trainFolderPath = "Images\\Train";
const testFolderPath = "Images\\Test";
const map = new Map();
const { Image } = require('canvas')
const trainImages = [];
const trainLabels = [];
const testImages = [];
const testLabels = [];
const imgHeight = 150;
const imgWidth = 150;
var timesToRun = 1000;
var model;
const NUM_OUTPUTS = 648;

// NUMBER OF CLASSES TO SELECT
const NUM_INPUTS = 5;
var INPUTS_TENSOR; 
var OUTPUTS_TENSOR;

// DETERMINES FIRST INDEX OF DATA THAT WILL BE CHOSEN
var rand = Math.floor(Math.random() * NUM_OUTPUTS);



function GetTestData(){
//     var fileIndex = 0;
// var Imagefolder = fs.readdirSync(testFolderPath);
// Imagefolder.forEach(folder => { 
//      console.log(folder + " " + fileIndex);
//      map.set(fileIndex,folder);
//      fileIndex = fileIndex + 1;
//     let folderLocation = testFolderPath.concat("\\", folder);
//         var Images = fs.readdirSync(folderLocation);
            
//                 Images.forEach(imageLocation => {
//                     let imagePath = folderLocation.concat("\\",imageLocation)
//                   //  console.log(imagePath); 
//                    var image = fs.readFileSync(imagePath);  
//                    var tensor = tf.node.decodePng(image,1).expandDims();
//                  // console.log(tensor.shape);
//                   testImages.push(tensor);
//                   testLabels.push(fileIndex);
                  
//                }); 
// } );
 }



 // Selects arbitrary amount of data to load by storing bools in a map
function SelectionMap(){
const selectMap = new Map();
var fileIndex = 0;

var Imagefolder = fs.readdirSync(trainFolderPath);
Imagefolder.forEach(folder => { 
selectMap.set(fileIndex,false);

  fileIndex = fileIndex + 1; 
});

var numberSelections = NUM_INPUTS;
var randtemp = rand;




while(numberSelections > 0){
console.log("has been selected at index" + rand);
selectMap.set(rand,true);
randtemp = (randtemp + Math.floor(Math.random() * NUM_OUTPUTS)) % NUM_OUTPUTS;
numberSelections--;
// Makes sure 1 element of randomly selected data will be apart of next batch
rand = randtemp;
}



return selectMap;
}



 function GetTrainData(){
    var fileIndex = 0;
    var Imagefolder = fs.readdirSync(trainFolderPath);
  
   const selectMap = SelectionMap();


    Imagefolder.forEach(folder => { 
         //console.log(folder + " " + fileIndex);
         map.set(fileIndex,folder);
   if(selectMap.get(fileIndex)){
    console.log(folder + " " + fileIndex);
        let folderLocation = trainFolderPath.concat("\\", folder);
            var Images = fs.readdirSync(folderLocation);
                
                    Images.forEach(imageLocation => {
                        let imagePath = folderLocation.concat("\\",imageLocation)
                      //  console.log(imagePath); 
                     var rand = Math.floor(Math.random() * 2);
                     //console.log(rand); 
                     if(rand == 0){
                       var image = fs.readFileSync(imagePath);  
                       var tensor = tf.node.decodePng(image,1).expandDims();
                    
                     //console.log(tensor);
                      trainImages.push(tensor);
                      trainLabels.push(fileIndex);
                     }
                   }); 
                  }
                   fileIndex = fileIndex + 1; } );                     
 }



 






async function train(){

let results = await model.fit(INPUTS_TENSOR,OUTPUTS_TENSOR,
 {
shuffle: true,
validationSplit: 0.2,
batchSize: 128,
epochs: 10,
});





await model.save('file://C:\Users\Administrator\app\model');


INPUTS_TENSOR.dispose();
OUTPUTS_TENSOR.dispose();


const path= "Images\\Test\\上\\Capture.png";
var image = fs.readFileSync(path); 
var tensor= tf.node.decodePng(image,1).expandDims();
//console.log(tensor);
let predictions =  model.predict(tensor);
predictions.print();



console.log("predicted " + predictions.squeeze().argMax());

const path2= "Images\\Test\\上\\Capture1.png";
var image2 = fs.readFileSync(path2); 
var tensor2= tf.node.decodePng(image2,1).expandDims();
//console.log(tensor);
let prediction2 =  model.predict(tensor2);
prediction2.print();
console.log("predicted " + prediction2.squeeze().argMax());


tensor.dispose();
tensor2.dispose();


if(timesToRun > 0){
  runModel();
  timesToRun--;
   }

}


async function loadModel(){
 model = await tf.loadLayersModel('file://C:\\Users\\Administrator\\app\\UsersAdministratorappmodel\\model.json');
 console.log(model.summary());
runModel();
}



function createModel(){
  
model = tf.sequential();

model.add(tf.layers.dense({inputShape: [150,150,1], units: 32, activation: 'relu'}));
  model.add(tf.layers.flatten());
  //model.add(tf.layers.dropout({ rate: 0.2 }));
model.add(tf.layers.dense({units: 16, activation: 'relu'}));
//model.add(tf.layers.dropout({ rate: 0.2 }));
model.add(tf.layers.batchNormalization());
model.add(tf.layers.dropout({ rate: 0.5 }));
model.add(tf.layers.dense({units: NUM_OUTPUTS, activation: 'softmax'}));

 // Pool data into a [2,2] matrix
  // model.add(tf.layers.maxPooling2d({poolSize: [2,2]}));
  // // Randomly selects neurons to omit 
  // model.add(tf.layers.dropout({rate: 0.2}));
  // model.add(tf.layers.flatten());
  // model.add(tf.layers.dense({units: 12, activation: 'relu'}));
  // model.add(tf.layers.dropout({rate: 0.3}));
  // model.add(tf.layers.dense({units: 2, activation: 'sigmoid'}));


 console.log(model.summary());

runModel(); 
}


function runModel(){


 GetTrainData();
const INPUTS = trainImages;
const OUTPUTS = trainLabels;


// Shuffle both Inputs and Outputs (making sure indices match)
//tf.util.shuffleCombo(INPUTS,OUTPUTS);



// Put all tensors into a single tensor as the input
INPUTS_TENSOR = tf.concat(trainImages); 

// Put all
 OUTPUTS_TENSOR = tf.oneHot(tf.tensor1d(OUTPUTS,'int32'),NUM_OUTPUTS);




  
    model.compile({
      optimizer: tf.train.adam(), 
      loss: 'categoricalCrossentropy', 
      metrics: ['accuracy'] 
    });


train();
  }




   // MAIN METHOD (nice one js)
  loadModel();
  //createModel();
