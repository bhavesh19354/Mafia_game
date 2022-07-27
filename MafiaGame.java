
/*Mafia- Strategic Multiplayer Game
Author: Abhimanyu Gupta (2019226) (abhimanyu19226@iiitd.ac.in)
Functionality: A program to simulate a game of Mafia having a single player controlled by user and others controlled by computer. 
For Purpose: Assignment 3, CSE201-Advanced Programming, Monsoon 2020 at IIIT-Delhi
Includes: MafiaGame.java - run and maintains the flow of the program
	MafiGame.class - calls the Game class's respective method to start the game
	Game.class - contains all the players playing the game and methods to play the game smoothly
	Character.class - an abstract class to store state of a player and acting as a base class for types of characters
	Commoner.class - a simple Character capable of just voting
	Mafia.class - a Character of killing in a group other Characters, other than voting
	Detective.class - a Character skilled with finding the Mafias(imposter among other Characters)
	Healer.class - a Character healing others, so as to survive Mafias' attack
*/

import java.util.Scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import java.lang.NumberFormatException;

abstract class Character
{
	private static int uniqueID;
	private final int id;
	private final String type;
	private double hp;
	private boolean alive;
	private boolean userControlled;

	static
	{
		Character.uniqueID=0;
	}
	public Character()
	{
		this.id=-1;
		this.type="Undefined";
		this.hp=-1;
		this.alive=false;
		this.userControlled=false;
	}
	public Character(double _hp)
	{
		++Character.uniqueID;
		this.id=Character.uniqueID;
		this.type=this.getClass().getName();
		this.hp=_hp;
		this.alive=true;
		this.userControlled=false;
	}

	//START of FUNCTIONS checking STATE
	public static void reset_unique_id()
	{
		Character.uniqueID=0;
	}

	public int get_id()
	{
		return this.id;
	}
	public String get_type()
	{
		return this.type;
	}
	public double get_hp()
	{
		return this.hp;
	}
	public boolean is_healthy()
	{
		return this.hp>0;
	}
	public boolean is_alive()
	{
		return this.alive;
	}
	public boolean is_dead()
	{
		return !this.is_alive();
	}
	public boolean is_user_controlled()
	{
		return this.userControlled;
	}
	//END of FUNCTIONS checking STATE
	
	//START of FUNCTIONS changing STATE
	public void make_user_controlled()
	{
		this.userControlled=true;
	}
	public void increase_hp(double gainHP)
	{
		this.hp+=gainHP;
	}
	public double reduce_hp(double lossHP)
	{
		this.hp-=lossHP;
		double excessHP=(this.hp<0)?(-this.hp):0;
		this.hp=(this.hp<0)?0:this.hp;
		return excessHP;
	}
	public void died()
	{
		this.hp=0;
		this.alive=false;
	}
	//END of FUNCTIONS changing STATE

	//START of OVERRDINGs
	@Override
	public String toString()
	{
		return "Player"+this.id;
	}
	@Override
	public boolean equals(Object other)
	{
		if(other!=null && this.getClass()==other.getClass())
		{
			Character second=(Character)other;
			return (this.id==second.id && this.hp==second.hp && this.type.equals(second.type) && this.alive==second.alive && this.userControlled==second.userControlled); 
		}
		else
			return false;
	}
	//END of OVERRIDINGs

	//START of DEBUGGING FUNCTIONS
	public void display_details()
	{
		System.out.println("["+this.id+" ("+this.type+") : "+this.hp+","+(this.alive?"ALIVE":"DEAD")+","+(this.userControlled?"USER":"COMPUTER")+"]");
	}
	//END of DEBUGGING FUNCTIONS

	//START of COMPARATOR for sorting on basis of hp
	static class CustomCompareHP implements Comparator<Character>
	{
		public int compare(Character o1,Character o2)
		{
			double hp1=o1.get_hp();
			double hp2=o2.get_hp();
			if(hp1<hp2)
				return -1;
			else if(hp1>hp2)
				return 1;
			return 0;
		}
	}
	//END of COMPARATOR
}

class Commoner extends Character
{
	public Commoner()
	{
		super(1000);
	}
}

class Mafia extends Character
{
	public Mafia()
	{
		super(2500);
	}
	public static void kill(ArrayList<Character> mafias,Character character)
	{
		double mafiasHP=0;
		for(Character mafia:mafias)
			mafiasHP+=mafia.get_hp();
		double hpReduced=Math.min(mafiasHP,character.get_hp());
		character.reduce_hp(hpReduced);
		Mafia.distribute_damage_recieved(mafias,hpReduced);
	}
	private static void distribute_damage_recieved(ArrayList<Character> mafias,double damageToAbsorb)
	{
		ArrayList<Character> sortedMafias=new ArrayList<Character>(mafias);
		Collections.sort(sortedMafias,new CustomCompareHP());
		int m=sortedMafias.size();
		for(int i=0;i<sortedMafias.size();++i)
		{
			double hpToReduce=damageToAbsorb/m;
			damageToAbsorb-=hpToReduce;
			damageToAbsorb+=sortedMafias.get(i).reduce_hp(hpToReduce);
			--m;
		}
	}
}

class Detective extends Character
{
	public Detective()
	{
		super(800);
	}
	public static boolean test_mafia(Character character)
	{
		return character.get_type().equals("Mafia");
	}
}

class Healer extends Character
{
	private static final double healingStrength;
	
	static
	{
		healingStrength=500;
	}

	public Healer()
	{
		super(800);
	}
	public static void heal(Character character)
	{
		character.increase_hp(Healer.healingStrength);
	}
}

class Game
{
	private final Scanner console;
	private int playersCount;
	private ArrayList<Character> allPlayers;
	private HashMap<String,ArrayList<Character>> alivePlayers;
	private TreeSet<Integer> aliveIDs;
	private Character userPlayer;
	private int rounds;
	private boolean debug;

	Game()
	{
		this.console=null;
		this.playersCount=-1;
		this.allPlayers=null;
		this.alivePlayers=null;
		this.aliveIDs=null;
		this.userPlayer=null;
		this.rounds=-1;
		this.debug=false;
	}
	Game(Scanner sc)
	{
		this.console=sc;
		this.playersCount=-1;
		this.allPlayers=null;
		this.alivePlayers=null;
		this.aliveIDs=null;
		this.userPlayer=null;
		this.rounds=0;
		this.debug=false;
	}

	public void actiavte_debuger()
	{
		this.debug=true;
	}

	//START of DRIVING FUNCTIONS
	public void new_game()
	{
		Character.reset_unique_id();

		System.out.println("Welcome to Mafia");
		this.input_number_of_players();
		this.assign_characters();
		if(this.debug)//DEBUGGER
			this.print_characters();
		this.input_user_player_type();
		this.show_user_player_and_allies();
		while(!this.game_over())
		{
			if(this.debug)//DEBUGGER
				this.print_characters();
			this.play_round();
		}
		if(this.debug)//DEBUGGER
			this.print_characters();
		this.game_results();
	}
	private void input_number_of_players()
	{
		boolean badInput=true;
		while(badInput)
		{
			System.out.print("Enter number of players: ");
			try
			{
				String line=console.nextLine();
				int count=Integer.parseInt(line);
				if(count<6)
					System.out.println("->Minimum 6 players supported");
				else
				{
					this.playersCount=count;
					badInput=false;
				}
			}
			catch(NumberFormatException e)
			{
				System.out.println("->Invalid Input Format (only integers accepted)");
			}
		}
	}
	private void assign_characters()
	{
		this.allPlayers=new ArrayList<Character>(this.playersCount);
		this.alivePlayers=new HashMap<String,ArrayList<Character>>();
		this.aliveIDs=new TreeSet<Integer>();
		int mafiasCount=this.playersCount/5;
		int detectivesCount=this.playersCount/5;
		int healersCount=Math.max(1,this.playersCount/10);
		int commonersCount=this.playersCount-(mafiasCount+detectivesCount+healersCount);
		ArrayList<Integer> players=new ArrayList<Integer>(this.playersCount);
		for(int i=0;i<mafiasCount;++i)
			players.add(0);
		for(int i=0;i<commonersCount;++i)
			players.add(1);
		for(int i=0;i<detectivesCount;++i)
			players.add(2);
		for(int i=0;i<healersCount;++i)
			players.add(3);
		Collections.shuffle(players);
		for(int i=0;i<this.playersCount;++i)
		{
			int type=players.get(i);
			Character character=null;
			if(type==0)
				character=new Mafia();
			else if(type==1)
				character=new Commoner();
			else if(type==2)
				character=new Detective();
			else if(type==3)
				character=new Healer();
			this.allPlayers.add(character);
			this.aliveIDs.add(character.get_id());
			ArrayList<Character> typeList=this.alivePlayers.get(character.get_type());
			if(typeList==null)
			{
				int[] count={mafiasCount,commonersCount,detectivesCount,healersCount};
				typeList=new ArrayList<Character>(count[type]);
				typeList.add(character);
				this.alivePlayers.put(character.get_type(),typeList);
			}
			else
				typeList.add(character);
		}
	}
	private void input_user_player_type()
	{
		boolean badInput=true;
		while(badInput)
		{
			System.out.println("Choose a Character");
			System.out.println("1) Mafia");
			System.out.println("2) Detective");
			System.out.println("3) Healer");
			System.out.println("4) Commoner");
			System.out.println("5) Assign Randomly");
			try
			{
				String line=console.nextLine();
				int choice=Integer.parseInt(line);
				if(choice<=0 || choice>=6)
					System.out.println("->Option Not available, enter corresponding integer only");
				else
				{
					String[] types={"Mafia","Detective","Healer","Commoner"};
					Random rd=new Random();
					if(choice==5)
						choice=rd.nextInt(4)+1;
					ArrayList<Character> players=this.alivePlayers.get(types[choice-1]);
					choice=rd.nextInt(players.size());
					players.get(choice).make_user_controlled();
					this.userPlayer=players.get(choice);
					badInput=false;
				}
			}
			catch(NumberFormatException e)
			{
				System.out.println("->Invalid Input Format (only integers accepted)");
			}
		}
	}
	private void show_user_player_and_allies()
	{
		System.out.println("You are Player"+this.userPlayer.get_id()+".");
		System.out.print("You are a "+userPlayer.get_type()+". ");
		if(this.userPlayer.get_type().equals("Commoner"))
		{
			System.out.println();
			return;
		}
		System.out.print("Other "+this.userPlayer.get_type()+"(s) are: ");
		ArrayList<Character> userTypeCharacters=new ArrayList<Character>(this.alivePlayers.get(this.userPlayer.get_type()));
		for(int i=0;i<userTypeCharacters.size();++i)
		{
			if(this.userPlayer.equals(userTypeCharacters.get(i)))
				userTypeCharacters.remove(this.userPlayer);
		}
		System.out.println(userTypeCharacters);
	}
	private void play_round()
	{
		++this.rounds;
		System.out.println("Round "+this.rounds+":");
		this.display_alive_players();
		int mafiaChoice=-1;
		if(this.alivePlayers.get("Mafia").size()!=0)
			mafiaChoice=this.choose_one(this.get_all_ids(this.alivePlayers.get("Mafia")),this.aliveIDs,Mafia.class);
		if(mafiaChoice!=-1)
			Mafia.kill(this.alivePlayers.get("Mafia"),this.allPlayers.get(mafiaChoice-1));
		int detectiveChoice=-1;
		if(this.alivePlayers.get("Detective").size()!=0 && !(this.alivePlayers.get("Detective").size()==1 && !this.alivePlayers.get("Detective").get(0).is_healthy()))
			detectiveChoice=this.choose_one(this.get_all_ids(this.alivePlayers.get("Detective")),this.aliveIDs,Detective.class);
		if(detectiveChoice!=-1 && this.userPlayer.is_alive() && this.userPlayer.get_type().equals("Detective"))
		{
			if(Detective.test_mafia(this.allPlayers.get(detectiveChoice-1)))
				System.out.println(this.allPlayers.get(detectiveChoice-1)+" is a Mafia");
			else
				System.out.println(this.allPlayers.get(detectiveChoice-1)+" is not a Mafia");
		}
		int healerChoice=-1;
		if(this.alivePlayers.get("Healer").size()!=0  && !(this.alivePlayers.get("Healer").size()==1 && !this.alivePlayers.get("Healer").get(0).is_healthy()))
			healerChoice=this.choose_one(this.get_all_ids(this.alivePlayers.get("Healer")),this.aliveIDs,Healer.class);
		if(healerChoice!=-1 && !(healerChoice==detectiveChoice && Detective.test_mafia(this.allPlayers.get(detectiveChoice-1))))
			Healer.heal(this.allPlayers.get(healerChoice-1));
		if(this.debug)//DEBUGGER
		{
			System.out.println("<------??------>");
			System.out.println("Mafia chose: "+mafiaChoice);
			System.out.println("Detective tested: "+detectiveChoice);
			System.out.println("Healer healed: "+healerChoice);
			System.out.println("<------?!?------>");
		}
		System.out.println("--End of actions--");
		if(mafiaChoice!=-1 && this.allPlayers.get(mafiaChoice-1).get_hp()==0)
		{
			this.allPlayers.get(mafiaChoice-1).died();
			System.out.println(this.allPlayers.get(mafiaChoice-1)+" has died.");
			this.aliveIDs.remove(this.allPlayers.get(mafiaChoice-1).get_id());
			this.alivePlayers.get(this.allPlayers.get(mafiaChoice-1).get_type()).remove(this.allPlayers.get(mafiaChoice-1));
		}
		else
			System.out.println("No one died.");
		int votingResult=-1;
		if(detectiveChoice!=-1 && Detective.test_mafia(this.allPlayers.get(detectiveChoice-1)))
			votingResult=detectiveChoice;
		else if(this.game_over())
			votingResult=-1;
		else
			votingResult=this.vote_out();
		if(votingResult!=-1)
		{
			System.out.println(this.allPlayers.get(votingResult-1)+" has been voted out.");
			this.allPlayers.get(votingResult-1).died();
			this.aliveIDs.remove(this.allPlayers.get(votingResult-1).get_id());
			this.alivePlayers.get(this.allPlayers.get(votingResult-1).get_type()).remove(this.allPlayers.get(votingResult-1));
		}
		System.out.println("--End of Round "+this.rounds+"--");
	}
	private boolean game_over()
	{
		if(this.alivePlayers.get("Mafia").size()==0)
			return true;
		else if((this.aliveIDs.size()-this.alivePlayers.get("Mafia").size())==0)
			return true;
		else if(this.alivePlayers.get("Mafia").size()==(this.aliveIDs.size()-this.alivePlayers.get("Mafia").size()))
			return true;
		else
			return false;
	}
	private void game_results()
	{
		if(!this.game_over())
		{
			System.out.println("Game in Progress.");
			System.out.println("Players alive: ");
			for(Character player:this.allPlayers)
			{
				if(!player.is_alive())
					continue;
				System.out.print(player);
				if(player.get_type().equals(this.userPlayer.get_type()) && !player.get_type().equals("Commoner"))
					System.out.print(" ("+player.get_type()+")");
				if(player.is_user_controlled())
					System.out.print(" [USER]");
				System.out.println();
			}
		}
		else
		{
			System.out.println("Game Over");
			boolean mafiasWon=this.alivePlayers.get("Mafia").size()!=0;
			if(mafiasWon)
				System.out.println("The Mafias have won");
			else
				System.out.println("The Mafia have lost");
			this.display_characters(Mafia.class);
			this.display_characters(Detective.class);
			this.display_characters(Healer.class);
			this.display_characters(Commoner.class);
		}
	}
	//END of DRIVING FUNCTIONS

	//START of HELPER FUNCTIONS
	private void display_alive_players()
	{
		System.out.print(this.aliveIDs.size()+" players are remaining: ");
		int i=0;
		for(Integer id:this.aliveIDs)
		{
			System.out.print(this.allPlayers.get(id-1));
			if(i==this.aliveIDs.size()-1)
				continue;
			else if(i==this.aliveIDs.size()-2)
				System.out.print(" and ");
			else
				System.out.print(", ");
			++i;
		}
		if(this.aliveIDs.size()==1)
			System.out.println(" is alive");
		else
			System.out.println(" are alive");
	}
	private int choose_one(ArrayList<Integer> charactersID,TreeSet<Integer> playersID,Class<? extends Character> characterType)
	{
		playersID=new TreeSet<Integer>(playersID);
		for(Integer id:charactersID)
		{
			if(playersID.contains(id))
				playersID.remove(id);
		}
		if(this.userPlayer.getClass().equals(characterType) && this.userPlayer.is_alive())
		{
			String message="Undefined";
			if(characterType.equals(Mafia.class))
				message="Choose a target: ";
			else if(characterType.equals(Detective.class))
				message="Choose a player to test: ";
			else if(characterType.equals(Healer.class))
				message="Choose a player to heal: ";
			return this.input_user_choice(playersID,message,characterType);
		}
		ArrayList<Integer> choices=new ArrayList<Integer>(playersID);
		Random rd=new Random();
		int choice=rd.nextInt(choices.size());
		if(characterType.equals(Mafia.class))
			System.out.println("Mafias have chosen their target.");
		else if(characterType.equals(Detective.class))
			System.out.println("Detectives have chosen a player to test.");
		else if(characterType.equals(Healer.class))
			System.out.println("Healers have chose someone to heal.");

		return choices.get(choice);
	}
	private int input_user_choice(TreeSet<Integer> players,String message,Class<? extends Character> characterType)
	{
		while(true)
		{
			System.out.print(message);
			try
			{
				String line=console.nextLine();
				int choice=Integer.parseInt(line);
				if(!players.contains(choice))
				{
					if(!this.aliveIDs.contains(choice))
					{
						if(choice>=1 && choice<=this.playersCount)
							System.out.println("->"+this.allPlayers.get(choice-1)+" is not in the game");
						else
							System.out.println("->"+choice+" do not correspond to a player");
					}
					else if(characterType.equals(Healer.class))
						return choice;
					else
					{
						if(characterType.equals(Mafia.class))
							System.out.println("->You can not target a "+characterType.getName());
						else if(characterType.equals(Detective.class))
							System.out.println("->You can not test a "+characterType.getName());
					}
				}
				else
					return choice;
			}
			catch(NumberFormatException e)
			{
				System.out.println("->Invalid Input Format (only integers accepted)");
			}
		}
	}
	private <T extends Character> void display_characters(Class<T> type)
	{
		ArrayList<T> characters=this.get_all_of(type);
		if(characters.size()==0)
			System.out.println("No "+type.getName()+"s were present");

		for(int i=0;i<characters.size();++i)
		{
			System.out.print(characters.get(i));
			if(characters.get(i).is_user_controlled())
				System.out.print("[User]");
			if(i==characters.size()-1)
				continue;
			else if(i==characters.size()-2)
				System.out.print(" and ");
			else
				System.out.print(", ");
		}
		if(characters.size()==1)
			System.out.println(" was "+type.getName());
		else
			System.out.println(" were "+type.getName()+"s");
	}
	private int vote_out()
	{
		ArrayList<Integer> options=new ArrayList<Integer>(this.aliveIDs);
		
		int votingResult=-1;
		while(votingResult==-1)
		{
			votingResult=this.voting_round(options);
			if(votingResult==-1)
				System.out.println("->Voting Tied, retaking votes");
		}
		return votingResult;
	}
	private int voting_round(ArrayList<Integer> options)
	{
		HashMap<Integer,Integer> votes=new HashMap<Integer,Integer>();
		for(int i=0;i<options.size();++i)
		{
			int p=options.get(i);
			int vote=i;
			if(this.allPlayers.get(p-1).equals(this.userPlayer))
			{
				boolean badInput=true;
				while(badInput)
				{
					System.out.print("Select a person to vote out: ");
					try
					{
						String line=console.nextLine();
						int choice=Integer.parseInt(line);
						if(this.aliveIDs.contains(choice))
						{
							vote=choice;
							badInput=false;
						}
						else if(choice>=1 && choice<=this.playersCount)
							System.out.println("->"+this.allPlayers.get(choice-1)+" is not in the game");
						else
							System.out.println("->"+choice+" do not correspond to a player");
					}
					catch(NumberFormatException e)
					{
						System.out.println("->Invalid Input Format (only integers accepted)");
					}
				}
			}
			else
			{
				Random rd=new Random();
				while(options.get(vote)==p)
					vote=rd.nextInt(options.size());
				vote=options.get(vote);
			}
			if(votes.containsKey(vote))
				votes.put(vote,votes.get(vote)+1);
			else
				votes.put(vote,1);
		}
		ArrayList<Map.Entry<Integer,Integer>> votesList=new ArrayList<Map.Entry<Integer,Integer>>(votes.entrySet());
		if(votesList.size()==1)
			return votesList.get(0).getKey();
		Collections.sort(votesList,new CustomCompareVotes());
		if(this.debug)//DEBUGGER
		{
			System.out.println("<------??------>");
			System.out.println("Voting Results:");
			for(Map.Entry<Integer,Integer> v:votesList)
				System.out.println(v.getKey()+" -> "+v.getValue());
			System.out.println("<------?!?------>");
		}
		if(votesList.get(0).getValue()==votesList.get(1).getValue())
			return -1;
		else
			return votesList.get(0).getKey();
	}

	private <T extends Character> ArrayList<T> get_all_of(Class<T> characterType)
	{	
		ArrayList<T> typeCharacters=new ArrayList<T>();
		for(Character player:this.allPlayers)
		{
			if(characterType.isInstance(player) && characterType.equals(player.getClass()))
				typeCharacters.add(characterType.cast(player));
		}
		return typeCharacters;
	}
	private ArrayList<Integer> get_all_ids(ArrayList<? extends Character> characters)
	{
		ArrayList<Integer> ids=new ArrayList<Integer>(characters.size());
		for(Character character:characters)
			ids.add(character.get_id());
		return ids;
	}
	//END of HELPER FUNCTIONS

	//START of DEBUGGING FUNCTIONS
	private void print_characters()
	{
		System.out.println("<------??------>");
		for(int i=0;i<this.playersCount;++i)
			this.allPlayers.get(i).display_details();
		System.out.println("<------?!?------>");
	}
	//END of DEBUGGING FUNCTIONS

	//START of COMPARATOR for sorting of votes
	static class CustomCompareVotes implements Comparator<Map.Entry<Integer,Integer>>
	{
		public int compare(Map.Entry<Integer,Integer> o1,Map.Entry<Integer,Integer> o2)
		{
			return o2.getValue().compareTo(o1.getValue());
		}
	}
	//END of COMPARATOR
}

public class MafiaGame
{
	public static void main(String[] args)
	{
		Scanner sc=new Scanner(System.in);
		
		boolean play=false;
		do
		{
			Game game=new Game(sc);
			if(args.length!=0 && args[0].equals("debug=true"))
				game.actiavte_debuger();
			game.new_game();
			System.out.println();
			boolean badInput=true;
			String choice="NO";
			int i=0;
			while(badInput)
			{
				System.out.print("::Do you want to play again? [Y/N]");
				System.out.println();
				choice=sc.nextLine();
				i=0;
				while(i<choice.length() && (choice.charAt(i)==' ' || choice.charAt(i)=='	' || choice.charAt(i)=='\n'))
					++i;
				badInput=(i==choice.length());
			}
			System.out.println();
			play=(choice.charAt(i)=='Y' || choice.charAt(i)=='y');
		}while(play);

		sc.close();
		return;
	}
}