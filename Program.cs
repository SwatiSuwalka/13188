using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.ProjectOxford.Face;
using Microsoft.ProjectOxford.Face.Contract;

namespace assure
{
    class Program: IDisposable
    {

        FaceServiceClient faceServiceClient = new FaceServiceClient("d349006973f6463793ef7e324971ee74", "https://westcentralus.api.cognitive.microsoft.com/face/v1.0");

        //creating parent group

        public async void CreatePersonGroup(String personGroupId, String personGroupName)
        {
            try
            {

                await faceServiceClient.CreatePersonGroupAsync(personGroupId, personGroupName);

            }
            catch (Exception ex)
            {
                Console.WriteLine("Error creating person group\n" + ex.Message);
            }

        }


        public async void AddPersonToGroup(String personGroupId, String name, String pathImage)
        {
            try
            {

                await faceServiceClient.GetPersonGroupAsync(personGroupId);
                CreatePersonResult person = await faceServiceClient.CreatePersonAsync(personGroupId, name);

                DetectFaceAndRegister(personGroupId,person, pathImage);


            }
            catch (Exception ex)
            {
                Console.WriteLine("Error Add person to the group\n" + ex.Message);
            }
        }

        private async void DetectFaceAndRegister(string personGroupId, CreatePersonResult person, string pathImage)
        {
            foreach (var imgPath in Directory.GetFiles(pathImage, "*.jpg"))

            {
                using (Stream s = File.OpenRead(imgPath))
                {
                    await faceServiceClient.AddPersonFaceAsync(personGroupId, person.PersonId, s);

                }
            }
        }

        public async void TrainingAI(String personGroupId)
        {
            await faceServiceClient.TrainPersonGroupAsync(personGroupId);
            TrainingStatus traningStatus = null;
            while (true)
            {
                traningStatus = await faceServiceClient.GetPersonGroupTrainingStatusAsync(personGroupId);
                if (traningStatus.Status != Status.Running)
                    break;
                await Task.Delay(1000);
            }
            Console.WriteLine("Training AI completed");
        }


        public async void RecognitionFace(String personGroupId, String imgPath)
        {

            using (Stream s = File.OpenRead(imgPath))
            {
                var faces = await faceServiceClient.DetectAsync(s);
                var faceIds = faces.Select(face => face.FaceId).ToArray();

                try
                {
                    var results = await faceServiceClient.IdentifyAsync(personGroupId, faceIds);
                    foreach (var identifyResults in results)
                    {
                        Console.WriteLine($"Results of Face:{identifyResults.FaceId}");
                        if (identifyResults.Candidates.Length == 0)
					  Console.WriteLine("No one Identified");
					else
					  {
                            var candidateId = identifyResults.Candidates[0].PersonId;
                            var person = await faceServiceClient.GetPersonAsync(personGroupId, candidateId);
                            Console.WriteLine($"Identified as {person.Name}");
                        }
                    }
                }
                catch (Exception ex)
                {
                    Console.WriteLine("Error Recognition Face : " + ex.Message);
                }
            }
        }


        static void Main(string[] args)
        {
            // new Program().CreatePersonGroup("friends", "Friends");

             new Program().AddPersonToGroup("friends", "Neeraj yadav", @"D:\pictures\startAction\neeraj");
             new Program().AddPersonToGroup("friends", "Ishan malhara", @"D:\pictures\startAction\ishan");
             new Program().AddPersonToGroup("friends", "Swati swalka" , @"D:\pictures\startAction\swati");

             new Program().TrainingAI("friends");

            new Program().RecognitionFace("friends", @"D:\pictures\ishan_test.jpg");
            Console.ReadLine();




        }

        public void Dispose()
        {
            throw new NotImplementedException();
        }
    }

        
    }
