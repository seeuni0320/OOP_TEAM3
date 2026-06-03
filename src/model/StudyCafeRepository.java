package model;

import java.io.*;
import java.util.*;

public class StudyCafeRepository {
    // 캡슐화 필드 선언
    private HashMap<String, User> userMap;
    private ArrayList<Seat> seatList;

    // 파일 저장 경로 정의
    private final String USER_FILE = "users.txt";
    private final String SEAT_FILE = "seats.txt";

    
    public StudyCafeRepository() {
        this.userMap = new HashMap<>();
        this.seatList = new ArrayList<>();
        loadData(); // 파일에서 데이터 읽어오기
    }

    // 데이터 조회 및 제공 메서 

    public HashMap<String, User> getUserMap() {
        return userMap;
    }

    public ArrayList<Seat> getSeatList() {
        return seatList;
    }

    //전화번호로 회원 검색하기 
    public User findUser(String phoneNumber) {
        return userMap.get(phoneNumber);
    }

    //신규 회원 저장하기
    public void saveUser(User user) {
        userMap.put(user.getPhoneNumber(), user);
    }

    // 파일 입출력(File I/O) 로직 (txt 파일 읽기/쓰기)

    //프로그램 시작 시 파일에서 데이터를 읽어와 컬렉션에 담는 메서드
    public void loadData() {
        // 1. 회원 정보 로드 (users.txt)
        File userFile = new File(USER_FILE);
        if (userFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(userFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // 데이터 포맷 예시: 01012345678,true,Time,2,0
                    String[] data = line.split(",");
                    if (data.length == 5) {
                        String phone = data[0];
                        boolean hasTicket = Boolean.parseBoolean(data[1]);
                        String type = data[2];
                        int hours = Integer.parseInt(data[3]);
                        int days = Integer.parseInt(data[4]);

                        User user = new User(phone);
                        user.setHasActiveTicket(hasTicket);
                        user.setActiveTicketType(type);
                        if (hours > 0) user.addRemainingHours(hours);
                        if (days > 0) user.addRemainingDays(days);

                        userMap.put(phone, user);
                    }
                }
            } catch (IOException e) {
                System.out.println("회원 정보 파일 로드 중 오류 발생: " + e.getMessage());
            }
        }

        // 2. 좌석 정보 로드 (seats.txt)
        File seatFile = new File(SEAT_FILE);
        if (seatFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(seatFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // 데이터 포맷 예시: 1,true,01012345678
                    String[] data = line.split(",");
                    if (data.length >= 2) {
                        int seatNum = Integer.parseInt(data[0]);
                        boolean occupied = Boolean.parseBoolean(data[1]);
                        
                        Seat seat = new Seat(seatNum);
                        seat.setOccupied(occupied);
                        if (occupied && data.length == 3) {
                            seat.setAssignedUserPhone(data[2]);
                        }
                        seatList.add(seat);
                    }
                }
            } catch (IOException e) {
                System.out.println("좌석 정보 파일 로드 중 오류 발생: " + e.getMessage());
            }
        } else {
            // 만약 seats.txt 파일이 아예 없다면 기본 좌석 16개를 강제로 생성
            for (int i = 1; i <= 16; i++) {
                seatList.add(new Seat(i));
            }
        }
    }

    // 프로그램 종료 시 최신 메모리 데이터를 파일에 덮어쓰는 메서드
     
    public void saveData() {
        // 1. 회원 정보 저장 (users.txt)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USER_FILE))) {
            for (User user : userMap.values()) {
                String line = String.format("%s,%b,%s,%d,%d",
                        user.getPhoneNumber(),
                        user.isHasActiveTicket(),
                        user.getActiveTicketType(),
                        user.getRemainingHours(),
                        user.getRemainingDays());
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("회원 정보 파일 저장 중 오류 발생: " + e.getMessage());
        }

        // 2. 좌석 정보 저장 (seats.txt)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(SEAT_FILE))) {
            for (Seat seat : seatList) {
                String line = String.format("%d,%b,%s",
                        seat.getSeatNumber(),
                        seat.isOccupied(),
                        seat.getAssignedUserPhone() == null ? "" : seat.getAssignedUserPhone());
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("좌석 정보 파일 저장 중 오류 발생: " + e.getMessage());
        }
    }
}
