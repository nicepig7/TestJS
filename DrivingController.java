public class DrivingController {	
	public class DrivingCmd{
		public double steer;
		public double accel;
		public double brake;
		public int backward;
		public String toString() {
			return steer+"/"+accel+"/"+brake+"/"+backward;
		}
	};
	int I = 0;
	boolean ISBRAKE = false;
	
	int BREAK_ABIL = 25;
	double ALMOST_ZERO = 0.01;
	
	
	
	public DrivingCmd controlDriving(double[] driveArray, double[] aicarArray, double[] trackArray, double[] damageArray, int[] rankArray, int trackCurveType, double[] trackAngleArray, double[] trackDistArray, double trackCurrentAngle){
		DrivingCmd cmd = new DrivingCmd();
		
		////////////////////// input parameters
		double toMiddle     = driveArray[DrivingInterface.drvie_toMiddle    ];
		double angle        = driveArray[DrivingInterface.drvie_angle       ];
		double speed        = driveArray[DrivingInterface.drvie_speed       ];

		double toStart				 = trackArray[DrivingInterface.track_toStart		];
		double dist_track			 = trackArray[DrivingInterface.track_dist_track		];
		double track_width			 = trackArray[DrivingInterface.track_width			];
		double track_dist_straight	 = trackArray[DrivingInterface.track_dist_straight	];
		int track_curve_type		 = trackCurveType;

		double[] tfa	= trackAngleArray;
		double[] tfd	= trackDistArray;
		//double track_current_angle		= trackCurrentAngle;
		
		double[] dist_cars = aicarArray;
		
		//double damage		 = damageArray[DrivingInterface.damage];
		//double damage_max	 = damageArray[DrivingInterface.damage_max];

		//int total_car_num	 = rankArray[DrivingInterface.rank_total_car_num	];
		//int my_rank			 = rankArray[DrivingInterface.rank_my_rank			];
		//int opponent_rank	 = rankArray[DrivingInterface.rank_opponent_rank	];		
		////////////////////// END input parameters
		
		// To-Do : Make your driving algorithm
		I++;
		//System.out.println("["+I++ +"]\t"+System.currentTimeMillis()%100000+ "\t"+(speed)+"\t "+toStart+"\t"+track_forward_dists[0]);
		
		
		
		// 변수 선언...
		double steer = 0;
		double accel = 1;
		double brake = 0;
		
		double fitPosition = 0;
		double midDiffRatio = (toMiddle-fitPosition)/track_width;
		
		steer = 0.5*(angle-midDiffRatio);
		
		// 1. 현재 상태
		double targetSpeed = MAX_SPEED;//angleToFitSpeed(currAng);
		double currAng = tfa[1]-tfa[0];
		
		// 1.1 직선주로
		if( isStraight(speed, track_dist_straight) ) {
			targetSpeed = MAX_SPEED;
		}
		// 1.2 앞에 코너 발견 - 감속구간
		else if( currAng == 0 ){
			// 1.2.1 얼마까지 줄여야 하낭?
			targetSpeed = calcNextSpeed(speed, toStart, tfa, tfd);
		}
		// 1.3 코너링 중에서
		else {
			targetSpeed = angleToFitSpeed(currAng);
		}
		
		
		double absMidDiffRatio = Math.abs(midDiffRatio);
		targetSpeed *=(1-absMidDiffRatio);
		
		
		// 1.3 calc Accel/Brake
		brake = getBrakeAmount(speed, targetSpeed);
		accel = getAccelAmount(speed, targetSpeed);
		
		System.out.println(r(currAng)+"\t"+r(toMiddle)+"\t"+r(targetSpeed)+"\t"+r(speed)+"\t"
				+r(accel)+"\t"+r(brake)+"\t"+r(steer));
		////////////////////// output values
		cmd.steer = steer;
		cmd.accel = accel;
		cmd.brake = brake;
		cmd.backward = DrivingInterface.gear_type_forward;
		////////////////////// END output values
		
		return cmd;
	}

	private double calcNextSpeed(double speed, double toStart, double[] tfa, double[] tfd) {
		double targetSpeed = MAX_SPEED;
		double[] tfDiffAngle = getTFDiffAngle(tfa);
		double currAng  = tfDiffAngle[0];
		
		
		for( int i=0, ii=tfDiffAngle.length ; i<ii ; i++ ) {
			double afterAng = tfDiffAngle[i];
			double distance = tfd[i] - toStart;
			
			if( currAng == afterAng ) {continue;}
			
			double afterSpeed = getFinalVelocity(distance, BREAK_ABIL, speed);
			double expSpeed = angleToFitSpeed(afterAng);
			
			
			if( afterSpeed > expSpeed ) {
				targetSpeed = expSpeed;
			}
			break;
		}
		return targetSpeed;
	}
	
	private boolean isStraight(double speed, double track_dist_straight) {
		double afterSpeed = getFinalVelocity(track_dist_straight, BREAK_ABIL, speed);
		return afterSpeed <= 0;
	}

	double MAX_SPEED = 60;
	double LOW_CORNER_SPEED = 15;
	
	private double getBrakeAmount(double speed, double targetSpeed) {
		double SMOOTH_SPEED_GAP = 5;
		if( speed < targetSpeed ) {return 0;}
		if( speed - SMOOTH_SPEED_GAP > targetSpeed ) {return 1;}
		return Math.sqrt((speed - targetSpeed)/SMOOTH_SPEED_GAP);
	}
	private double getAccelAmount(double speed, double targetSpeed) {
		double SMOOTH_SPEED_GAP = 5;
		if( MAX_SPEED == targetSpeed ) { return 1;}
		if( targetSpeed < speed ) {return 0;}
		if( targetSpeed - SMOOTH_SPEED_GAP > speed ) {return 1;}
		return Math.sqrt((targetSpeed - speed)/SMOOTH_SPEED_GAP);
	}
	
	private double r(double d) {
		final int SIZE = 1000;
		return (double)(Math.round(d*SIZE))/SIZE;
	}
	
	private double getFinalVelocity(double s, double a, double v) {
		if(v*v<2*s*a) {return 0;}
		double t = (v - Math.sqrt(v*v-2*s*a))/a;
		return v-a*t;
	}
	
	private double angleToFitSpeed(double angle) {
		double absAngle = Math.abs(angle);
		if(absAngle>0.1) {
			return LOW_CORNER_SPEED;
		}
		return MAX_SPEED-absAngle*10*(MAX_SPEED-LOW_CORNER_SPEED);
	}
	
	
	

	private double[] getTFDiffAngle(double[] tfa) {
		int tfa_len = tfa.length;
		double[] tfDiffAngle = new double[tfa_len-1];
		for( int i=0, ii=tfa_len ; i<ii-1 ; i++ ) {
			tfDiffAngle[i] = tfa[i+1] - tfa[i];
		}
		return tfDiffAngle;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static void main(String[] args) {
		DrivingInterface driving = new DrivingInterface();
		DrivingController controller = new DrivingController();
		
		double[] driveArray = new double[DrivingInterface.INPUT_DRIVE_SIZE];
		double[] aicarArray = new double[DrivingInterface.INPUT_AICAR_SIZE];
		double[] trackArray = new double[DrivingInterface.INPUT_TRACK_SIZE];
		double[] damageArray = new double[DrivingInterface.INPUT_DAMAGE_SIZE];
		int[] rankArray = new int[DrivingInterface.INPUT_RANK_SIZE];
		int[] trackCurveType = new int[1];
		double[] trackAngleArray = new double[DrivingInterface.INPUT_FORWARD_TRACK_SIZE];
		double[] trackDistArray = new double[DrivingInterface.INPUT_FORWARD_TRACK_SIZE];
		double[] trackCurrentAngle = new double[1];
				
		// To-Do : Initialize with your team name.
		int result = driving.OpenSharedMemory();
		
		if(result == 0){
			boolean doLoop = true;
			int i=0;
			while(doLoop){
				result = driving.ReadSharedMemory(driveArray, aicarArray, trackArray, damageArray, rankArray, trackCurveType, trackAngleArray, trackDistArray, trackCurrentAngle);
				switch(result){
				case 0:
					DrivingCmd cmd = controller.controlDriving(driveArray, aicarArray, trackArray, damageArray, rankArray, trackCurveType[0], trackAngleArray, trackDistArray, trackCurrentAngle[0]);
					driving.WriteSharedMemory(cmd.steer, cmd.accel, cmd.brake, cmd.backward);
					break;
				case 1:
					break;
				case 2:
					// disconnected
				default:
					// error occurred
					doLoop = false;
					break;
				}
			}
		}
	}
}
