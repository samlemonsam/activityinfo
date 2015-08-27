using NUnit.Framework;
using System;
using ActivityInfo.Client;

namespace ActivityInfo.Client.Test
{
	[TestFixture ()]
	public class Test
	{
		[Test ()]
		public void TestCreateSite ()
		{

			SiteBuilder site = new SiteBuilder ();
			site.SetActivityId (11414);
			site.SetPartnerId (2156);
			site.SetLocationId (1328978624);
			site.SetStartDate (new DateTime (2015, 1, 1));
			site.SetEndDate (new DateTime (2015, 1, 31));
			site.SetIndicatorValue (1380119412, 1024);
			site.SetComments ("Created through the .NET client");
			//site.setIndicatorValue (5335, 1025);
			//site.SetAttribute (1556, true);
			//site.SetAttribute (1557, true);
			//site.SetAttribute (1560, true);

			// TODO: need to be able to create a new user and set up database to
			// allow for truly repeatable testing.
			ActivityInfoClient client = new ActivityInfoClient ();
			client.CreateSite (site); 
		}
	}
}

