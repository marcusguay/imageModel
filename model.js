

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

 function GetTrainData(){
    var fileIndex = 0;
    var Imagefolder = fs.readdirSync(trainFolderPath);
    Imagefolder.forEach(folder => { 
         console.log(folder + " " + fileIndex);
         map.set(fileIndex,folder);
         fileIndex = fileIndex + 1;
        let folderLocation = trainFolderPath.concat("\\", folder);
            var Images = fs.readdirSync(folderLocation);
                
                    Images.forEach(imageLocation => {
                        let imagePath = folderLocation.concat("\\",imageLocation)
                      //  console.log(imagePath); 
                       var image = fs.readFileSync(imagePath);  
                       var tensor = tf.node.decodePng(image,1).expandDims();
                     //console.log(tensor);
                      trainImages.push(tensor);
                      trainLabels.push(fileIndex);
                      
                   }); 
    } );
         

 }



 






async function train(){

let results = await model.fit(INPUTS_TENSOR,OUTPUTS_TENSOR,
 {
shuffle: true,
validationSplit: 0.2,
batchSize: 64,
epochs: 50,
});










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


}



 // Main method

 GetTrainData();
 //GetTestData();
const INPUTS = trainImages;
const OUTPUTS = trainLabels;


// Shuffle both Inputs and Outputs (making sure indices match)
tf.util.shuffleCombo(INPUTS,OUTPUTS);



// Put all tensors into a single tensor as the input
const INPUTS_TENSOR = tf.concat(trainImages); 

// Put all
const OUTPUTS_TENSOR = tf.oneHot(tf.tensor1d(OUTPUTS,'int32'),2);



//console.log(INPUTS_TENSOR);
console.log(OUTPUTS_TENSOR);

const model = tf.sequential();

model.add(tf.layers.conv2d({
    inputShape: [150, 150, 1],
    filters: 32,
    kernelSize: [3,3],
    activation: 'relu',
  }));
 // Pool data into a [2,2] matrix
  model.add(tf.layers.maxPooling2d({poolSize: [2,2]}));
  // Randomly selects neurons to omit 
  model.add(tf.layers.dropout({rate: 0.2}));
  model.add(tf.layers.flatten());
  model.add(tf.layers.dense({units: 12, activation: 'relu'}));
  model.add(tf.layers.dropout({rate: 0.3}));
  model.add(tf.layers.dense({units: 2, activation: 'softmax'}));
 

  model.summary();

  model.compile({
    optimizer: tf.train.adam(),
    loss: 'categoricalCrossentropy',
    metrics : ['accuracy']
});




train();
var testData = GetTestData();



