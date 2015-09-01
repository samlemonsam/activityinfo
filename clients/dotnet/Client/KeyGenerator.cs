using System;

namespace ActivityInfo.Client
{
	public class KeyGenerator
	{
		private static Random random = new Random();

		public KeyGenerator ()
		{
		}
		/// <summary>
		/// Generates a new id for an ActivityInfo entity
		/// </summary>
		/// <returns>The identifier.</returns>
		public static int GenerateId() {
			return random.Next (1, 2 ^ 31);
		}
	}
}

