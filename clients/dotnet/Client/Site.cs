using System;
using System.Collections.Generic;
using Newtonsoft.Json;

namespace ActivityInfo.Client
{
	public class Site
	{
		public int Id { get; set; }

		[JsonProperty("activity")]
		public int ActivityId { get; set; }

		/// <summary>
		/// Gets or sets identifiers of the attributes that have been set for this site.
		/// </summary>
		/// <value>The attribute identifiers.</value>
		[JsonProperty("attributes")]
		public List<int> AttributeIds { get; set; }

		[JsonProperty]
		public Dictionary<int, Object> IndicatorValues { get; set;  }	

		[JsonProperty]
		public string Comments { get; set ;}

		public Site ()
		{
		}
	}
}

